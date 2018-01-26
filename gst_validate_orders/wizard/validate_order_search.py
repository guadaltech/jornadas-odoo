# -*- encoding: utf-8 -*-
##############################################################################
#
#    Guadaltech Soluciones tecnológicas S.L.  www.guadaltech.es
#    Author: Carlos Miras
#    Copyright (C) 2004-2010 Tiny SPRL (http://tiny.be). All Rights Reserved
#    
#
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see http://www.gnu.org/licenses/.
#
##############################################################################


from openerp.osv import osv, fields
from openerp.tools.translate import _
from datetime import datetime

def numero(x):
    
    if not x:
        return "ZZZZ"
        
    code = x.split('-')[0]
    if not code or not code.isdigit():
        return code
    if code.isdigit():
        if '-' not in x:
            return x + '00'
        return x.replace('-','')
    return x

class validate_order_search(osv.osv_memory):
    _name = "validate.order.search"
    _description = "Search Validate Orders"

    _columns = {
       'search_field': fields.char('Ref Pedido', size=24, required=True),
    }
    
    def _actualiza(self,cr,uid,id):
        obj_validarte = self.pool.get('order.validation')
        obj_validarte_line = self.pool.get('order.validation.line')
        
        validate = obj_validarte.browse(cr,uid,id)
        lines = validate.sale_order_id.order_line
            
        ordered_lines = sorted(lines, key=lambda lines: numero(lines.product_id.default_code) or "ZZ")         
        order_line_product_ids_dict = {}
        order_line_product_ids=[]
        for line in ordered_lines:
            if line.product_id and line.product_id.type == 'product':
                if line.product_id.id not in order_line_product_ids_dict.keys():
                    order_line_product_ids_dict.update({line.product_id.id:line.product_uom_qty})
                    order_line_product_ids.append(line.product_id.id)
                else:
                    order_line_product_ids_dict[line.product_id.id] += line.product_uom_qty
        validation_line_product_ids = [line.product_id.id for line in validate.product_lines]
        
        remove_list = list(set(validation_line_product_ids) - set(order_line_product_ids))
        add_list = list(set(order_line_product_ids) - set(validation_line_product_ids))
        update_list = list(set(order_line_product_ids) & set(validation_line_product_ids))
        
        for product_id in remove_list:
            id = obj_validarte_line.search(cr,uid,[('order_id','=',validate.id),('product_id','=',product_id),('product_id.embalaje','=',False)])
            if id:
                obj_validarte_line.write(cr,uid, id, {'qty':0,'sec':-1,})
        
        cont = 0
        for product_id, qty in order_line_product_ids_dict.iteritems():
            # order_line_id = obj_sale_line.search(cr,uid,[('order_id','=',validate.sale_order_id.id),('product_id','=',product_id)])
            # order_line = obj_sale_line.browse(cr,uid,order_line_id[0])
            if product_id in update_list:
                id = obj_validarte_line.search(cr,uid,[('order_id','=',validate.id),('product_id','=',product_id)])
                obj_validarte_line.write(cr,uid, id, {'qty':qty,'sec':cont,})
            elif product_id in add_list:
                obj_validarte_line.create(cr,uid,{'order_id':validate.id,
                                                                        'product_id':product_id,
                                                                        'qty':qty,
                                                                        'sec':cont,
                                                       })
            cont += 1
                
    
    def search(self, cr, uid, ids, context=None):
        for s in self.browse(cr, uid, ids):
            domain = [('name','=', s.search_field)]
            search_ids = self.pool.get('order.validation').search(cr, uid, domain)
            if len(search_ids) == 1:
                type = 'kanban,page,form,tree'
                self._actualiza(cr,uid,search_ids[0])
            else:
                type = 'tree,form'                    
            
            res_id = self.pool.get('ir.ui.view').search(cr,uid,[
                                                    ('model','=','order.validation.line'),
                                                    ('name','=','validation.order.line.kanban.gst')
                                                    
                                                    ])
            if len(search_ids) == 0:
                sale_order_id = self.pool.get('sale.order').search(cr,uid,[('name','=', s.search_field)])
                if len(sale_order_id) == 1:
                    order_validation_id = self.pool.get('order.validation').create(cr,uid,{'name':s.search_field,
                                                                                           'sale_order_id':sale_order_id[0],
                                                                                           'start_date': datetime.now().strftime(
                                                                                               '%Y-%m-%d %H:%M:%S'),

                                                                                           })

                    for sale in self.pool.get('sale.order').browse(cr,uid,sale_order_id):
                        self.pool.get("sale.order").write(cr, uid, sale.id, {'order_validation':order_validation_id })

                        cont = 0
                        lines = sale.order_line
                        ordered_lines = sorted(lines, key=lambda lines: numero(lines.product_id.default_code) or "ZZ")
                        order_line_product_ids_dict = {}
                        order_line_product_ids=[]
                        for line in ordered_lines:
                            if line.product_id and line.product_id.type == 'product':
                                if line.product_id.id not in order_line_product_ids_dict.keys():
                                    order_line_product_ids_dict.update({line.product_id.id:line.product_uom_qty})
                                    order_line_product_ids.append(line.product_id.id)
                                else:
                                    order_line_product_ids_dict[line.product_id.id] += line.product_uom_qty

                        for product_id in order_line_product_ids:
                            self.pool.get('order.validation.line').create(cr,uid,{'order_id':order_validation_id,
                                                                        'product_id':product_id,
                                                                        'qty':order_line_product_ids_dict[product_id],
                                                                        'sec':cont,
                                                       })
                            cont += 1
                    return {
                        'name': _("PEDIDO "+ s.search_field),
                        'view_type': 'form',
                        'view_mode': 'kanban',
                        'view_id' : res_id,
                        'res_model': 'order.validation.line',
                        'type': 'ir.actions.act_window',
                        'domain' : [('order_id','=',order_validation_id)],
                        'context':{"search_default_validate":1},
            
                    }
                raise osv.except_osv(_('Warning !'), 'No existe ningún pedido con esta referencia.')
                
                
                
           
            return {
                'name': _("PEDIDO "+ s.search_field),
                'view_type': 'form',
                'view_mode': 'kanban',
                'view_id' : res_id,
                'res_model': 'order.validation.line',
                'type': 'ir.actions.act_window',
                'domain' : [('order_id','=',search_ids[0])],
                'context':{"search_default_validate":1},
    
            }
            

validate_order_search()

# vim:expandtab:smartindent:tabstop=4:softtabstop=4:shiftwidth=4:
