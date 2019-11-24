package com.elewise.ldmobile.model

data class DocumentItem (
  val line_num: String,
  val description: String,
  val item_code: String,
  val pack_view: String,
  val uom: String,
  val quan_place: String,
  val weight_br: String,
  val quantity: String,
  val price: String,
  val amount_without_tax: String,
  val tax: String,
  val tax_amount: String,
  val amount_with_tax: String
)