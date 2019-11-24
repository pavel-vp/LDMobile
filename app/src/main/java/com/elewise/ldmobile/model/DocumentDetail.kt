package com.elewise.ldmobile.model

data class DocumentDetail (
   val doc_type: String,
   val doc_id: Int,
   val doc_name: String,
   val vendor_name: String,
   val direction: String,
   val action_type: String,
   val sup_org: String?,
   val ship_org: String?,
   val cons_org: String?,
   val payer_org: String?,
   val perf_org: String?,
   val cust_org: String?,
   val vendor_org: String?,
   val buyer_org: String?,
   val reason: String?,
   val cfo: String?,
   val total_amount_without_tax: String?,
   val total_tax_amount: String?,
   val total_amount_with_tax: String?,
   val items: Array<DocumentItem>?,
   val history: Array<DocumentHistory>?,
   val attachments: Array<DocumentAttachment>?
)

