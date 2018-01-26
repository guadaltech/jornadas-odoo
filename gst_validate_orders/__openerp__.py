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


{
    'name': 'Validate Orders with Barcode',
    'version': '6.1.0',
    'category': '',
    'description': """ Validación de pedidos de ventas/compras con lector de código de barras  """,
    'author': 'Alberto Martín Cortada - Guadaltech',
    'website': 'http://www.guadaltech.es',
    'depends': ['web', 'purchase', 'sale'],
    'init_xml': [],
    'update_xml': ['order_validation_view.xml',
                   'wizard/validate_order_search.xml',
                   'report/report_order.xml',
                   'sale_view.xml'],
    'demo_xml': [],
    'test': [
    ],

    'js': [
        'static/src/js/view.js',
    ],
    'installable': True,
    'active': False,
    'certificate': False,
    'application': False,
    'auto_install': False,
}
