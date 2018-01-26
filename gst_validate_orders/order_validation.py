# -*- encoding: utf-8 -*-
##############################################################################
#
#    Guadaltech Soluciones Tecnologicas S.L www.guadaltech.es
#       Author: Alberto Martín Cortada   
#    Copyright (C) 2011-12 Tiny SPRL (http://tiny.be). All Rights Reserved
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

from osv import osv, fields
from tools.translate import _

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
        return x.replace('-', '')
    return x


class ProductProduct(osv.osv):
    _inherit = "product.product"
    _columns = {
        'embalaje': fields.boolean("Embalaje"),
        'ean_pack': fields.char('EAN Pack', size=80),
    }


class OrderValidation(osv.osv):
    _name = 'order.validation'

    def _actualiza(self, cr, uid, id):
        obj_validarte = self.pool.get('order.validation')
        obj_validarte_line = self.pool.get('order.validation.line')

        validate = obj_validarte.browse(cr, uid, id)
        # obj_sale = self.pool.get('sale.order')
        # obj_sale_line = self.pool.get('sale.order.line')
        lines = validate.sale_order_id.order_line

        ordered_lines = sorted(lines, key=lambda lines: numero(lines.product_id.default_code) or "ZZ")
        order_line_product_ids_dict = {}
        order_line_product_ids = []
        for line in ordered_lines:
            if line.product_id and line.product_id.type == 'product':
                if line.product_id.id not in order_line_product_ids_dict.keys():
                    order_line_product_ids_dict.update({line.product_id.id: line.product_uom_qty})
                    order_line_product_ids.append(line.product_id.id)
                else:
                    order_line_product_ids_dict[line.product_id.id] += line.product_uom_qty
        # order_line_product_ids = [line.product_id.type == 'product' and line.product_id.id for line in ordered_lines]
        # while False in order_line_product_ids:
        #     order_line_product_ids.remove(False)
        validation_line_product_ids = [line.product_id.id for line in validate.product_lines]

        remove_list = list(set(validation_line_product_ids) - set(order_line_product_ids))
        add_list = list(set(order_line_product_ids) - set(validation_line_product_ids))
        update_list = list(set(order_line_product_ids) & set(validation_line_product_ids))

        for product_id in remove_list:
            id = obj_validarte_line.search(cr, uid, [('order_id', '=', validate.id), ('product_id', '=', product_id),
                                                     ('product_id.embalaje', '=', False)])
            if id:
                obj_validarte_line.write(cr, uid, id, {'qty': 0, 'sec': -1, })
            #             obj_validarte_line.unlink(cr,uid, id)

        cont = 0
        for product_id in order_line_product_ids:
            # order_line_id = obj_sale_line.search(cr,uid,[('order_id','=',validate.sale_order_id.id),('product_id','=',product_id)])
            # order_line = obj_sale_line.browse(cr,uid,order_line_id[0])
            if product_id in update_list:
                id = obj_validarte_line.search(cr, uid,
                                               [('order_id', '=', validate.id), ('product_id', '=', product_id)])
                obj_validarte_line.write(cr, uid, id, {'qty': order_line_product_ids_dict[product_id], 'sec': cont, })
            elif product_id in add_list:
                obj_validarte_line.create(cr, uid, {'order_id': validate.id,
                                                    'product_id': product_id,
                                                    'qty': order_line_product_ids_dict[product_id],
                                                    'sec': cont,
                                                    })
            cont += 1

    def _get_progress(self, cr, uid, ids, fields, arg, context=None):
        result = {}
        for order in self.browse(cr, uid, ids, context=context):
            qty_total = sum([x.qty for x in order.product_lines])
            qty_to_validate_total = sum([x.qty_to_validate for x in order.product_lines])
            result[order.id] = {
                'qty_total': qty_total,
                'qty_to_validate_total': qty_to_validate_total,
                'validation': 'C' if (qty_total == qty_to_validate_total) else (
                    'A' if (qty_to_validate_total == 0) else 'B')
            }
        return result

    _columns = {
        'start_date': fields.datetime(string="Fecha Inicio"),
        'end_date': fields.datetime(string="Fecha Final"),
        'name': fields.char('Referencia Pedido', size=32),
        'sale_order_id': fields.many2one('sale.order', 'Pedido de Venta'),
        'code': fields.char('Código', size=64),
        'product_lines': fields.one2many('order.validation.line', 'order_id', string='Productos'),
        'qty_total': fields.function(_get_progress, string='Qty total', type='integer', readonly=True, store=False,
                                     multi='sums'),
        'qty_to_validate_total': fields.function(_get_progress, string='Qty to validate total', type='integer',
                                                 readonly=True, store=False, multi='sums'),
        'validation': fields.function(_get_progress, method=True, type='selection', selection=[
            ('A', 'No validado'),
            ('B', 'Parcialmente validado'),
            ('C', 'Validado')], string='Estado de validación', readonly=True, store=False, multi='sums'),
    }
    _default = {
        'validation': 0,
    }

    def open_kanban_product(self, cr, uid, ids, context):

        res_id = self.pool.get('ir.ui.view').search(cr, uid, [
            ('model', '=', 'order.validation.line'),
            ('name', '=', 'validation.order.line.kanban.gst')

        ])
        self._actualiza(cr, uid, ids[0])
        order = self.browse(cr, uid, ids[0], context=context)
        return {
            'name': _("PEDIDO " + order.name),
            'view_type': 'form',
            'view_mode': 'kanban',
            'view_id': res_id,
            'res_model': 'order.validation.line',
            'type': 'ir.actions.act_window',
            'domain': [('order_id', '=', ids[0])],
            'context': {"search_default_validate": 1},

        }

    def actualizar(self, cr, uid, ids, context):
        self._actualiza(cr, uid, ids[0])
        return True

    def validate_product(self, cr, uid, ids, value, qty_validation, context=None):
        try:
            return self.pool.get('order.validation.line').validate(cr, uid, ids, qty_validation)
        except:
            return False


class OrderValidationLine(osv.osv):
    _name = 'order.validation.line'
    _order = 'sec'

    def _get_image(self, cr, uid, ids, names, args, context=None):
        res = {}
        for order in self.browse(cr, uid, ids, context=context):
            res[order.id] = order.product_id.product_image
        return res

    def get_state(self, cr, uid, ids, field_name, arg, context={}):
        res = {}
        for line in self.browse(cr, uid, ids, context):
            validation = 'B'
            if line.qty_to_validate == line.qty:
                validation = 'C'
            elif line.qty_to_validate == 0:
                validation = 'A'
            res[line.id] = validation
        return res

    _columns = {
        'move_id': fields.many2one("stock.move", 'Movimiento'),
        'order_id': fields.many2one('order.validation', 'Validación'),
        'product_id': fields.many2one('product.product', 'Producto', required=True),
        'ean13': fields.related('product_id', 'ean13', type="char", relation='product.product', string="EAN13"),
        'ean_pack': fields.related('product_id', 'ean_pack', type="char", relation='product.product',
                                   string="EAN Pack"),
        'product_image': fields.function(_get_image, string='Imagen', type='binary', readonly=True, store=False),
        'qty': fields.integer("Cantidad Pedido"),
        'sec': fields.integer("Secuencia"),
        'qty_to_validate': fields.integer("Cantidad Validada"),

        'validation': fields.function(get_state, method=True, type='selection', selection=[
            ('A', 'No validado'),
            ('B', 'Parcialmente validado'),
            ('C', 'Validado')], string='Estado de validación', readonly=True, store=True),
    }

    _defaults = {
        'qty_to_validate': 0,

    }

    def find_consumible(self, cr, uid, id, ean13, context=None):
        if ean13:
            prod_ids = self.pool.get('product.product').search(cr, uid,
                                                               [('ean13', '=', ean13), ('embalaje', '=', True)])
            if len(prod_ids) == 1:
                line_ids = self.search(cr, uid, [('product_id', 'in', prod_ids), ('order_id', '=', id)])
                if len(line_ids) == 1:
                    return [prod_ids[0], line_ids[0]]
                return prod_ids[0]
        return False

    def create_consumible(self, cr, uid, ids, prod_id, qty, context=None):
        line_id = self.create(cr, uid, {'order_id': ids[0],
                                        'product_id': prod_id,
                                        'qty': qty,
                                        'qty_to_validate': qty,
                                        'validation': 'C',
                                        'sec': -99})
        return line_id

    def create_picking(self, cr, uid, order_id, context):

        for embalaje in self.browse(cr, uid, self.search(cr, uid, [('order_id', '=', order_id.id),
                                                                   ('product_id.embalaje', '=', True)])):

            if embalaje.move_id:
                if embalaje.move_id.product_qty != embalaje.qty:
                    cr.execute(
                        "UPDATE stock_move SET product_qty = %s WHERE id = %s" % (embalaje.qty, embalaje.move_id.id))
            else:
                source = dest = product_uom = None
                for picking in embalaje.order_id.sale_order_id.picking_ids:
                    for move_line in picking.move_lines:
                        source = move_line.location_id.id
                        dest = move_line.location_dest_id.id
                        product_uom = move_line.product_uom.id
                        break
                    break

                data = {
                    'name': 'EMB %s: %s' % (embalaje.order_id.name, embalaje.product_id.name),
                    'product_id': embalaje.product_id.id,
                    'product_qty': embalaje.qty,
                    'product_uom': product_uom,
                    'location_id': source,
                    'location_dest_id': dest,
                    'state': 'done',
                }

                move_id = self.pool.get('stock.move').create(cr, uid, data)
                self.write(cr, uid, [embalaje.id], {'move_id': move_id}, context)
            return True

    def validate(self, cr, uid, ids, qty_validation, ean13=None, context=None):
        res = []
        for line in self.browse(cr, uid, ids, context):
            order_id = line.order_id
            if line.product_id.embalaje == True and line.product_id.type == 'consu':
                self.write(cr, uid, [line.id],
                           {'qty_to_validate': line.qty_to_validate + qty_validation, 'qty': line.qty + qty_validation},
                           context)
            else:
                validation = 'A'
                if qty_validation > (line.qty - line.qty_to_validate) or (qty_validation + line.qty_to_validate) < 0:
                    return line.qty - line.qty_to_validate
                if line.qty_to_validate + qty_validation == line.qty:
                    validation = 'C'
                elif line.qty_to_validate + qty_validation < line.qty and line.qty_to_validate + qty_validation > 0:
                    validation = 'B'
                self.write(cr, uid, [line.id],
                           {'qty_to_validate': line.qty_to_validate + qty_validation, 'validation': validation},
                           context)

            if line.order_id.validation == 'C':
                res.append(-1)
                self.create_picking(cr, uid, order_id, context)
            res.append(line.id)

            if order_id and order_id.validation == 'C':
                self.pool.get("order.validation").write(cr, uid, order_id.id, {
                    'end_date': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
                })

        return res


class SaleOrder(osv.osv):
    _inherit = 'sale.order'

    def get_state(self, cr, uid, ids, field_name, arg, context={}):
        res = {}
        for id in ids:
            res[id] = 'A'
        order_validation_ids = self.pool.get('order.validation').search(cr, uid, [('sale_order_id', 'in', ids)])

        for order_v in self.pool.get('order.validation').browse(cr, uid, order_validation_ids, context):
            res[order_v.sale_order_id.id] = order_v.validation

        return res

    _columns = {
        'order_validation': fields.many2one("order.validation", "Validación"),
        'start_date_validation': fields.related("order_validation", 'start_date', type="datetime"),
        'end_date_validation': fields.related("order_validation", 'end_date', type="datetime"),
        'validation': fields.function(get_state, method=True, type='selection', selection=[
            ('A', 'No validado'),
            ('B', 'Parcialmente validado'),
            ('C', 'Validado')], string='Estado de validación', readonly=True, store=False),

    }

    def write(self, cr, uid, ids, vals, context=None):

        res = super(SaleOrder, self).write(cr, uid, ids, vals, context)

        if 'order_line' in vals.keys():

            if not isinstance(ids, list):
                ids = [ids]

            search_validates = self.pool.get("order.validation").search(cr, uid, [('sale_order_id', 'in', ids),
                                                                                  ('validation', '=', 'C')])
            if search_validates:
                self.pool.get("order.validation").actualizar(cr, uid, search_validates, context)

        return res
