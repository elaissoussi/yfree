

new EncrypteFields(cryptoService, namedJdbcTemplate, log).encrypt()

class EncrypteFields {

    private List<RecipientTuple> fetchRecipients() {
        return namedJdbcTemplate.query("""
                        SELECT id , 
                               recipient #>> '{organization,identificationNumber }' AS organization_identification_number,
                               recipient #>> '{organization, taxIdentificationNumber}' AS organization_tax_identification_number,
                               recipient #>> '{billingAddress,street1}' AS billing_address_street1,
                               recipient #>> '{billingAddress,street2}' AS billing_address_street2
                        FROM document_request
                        AND recipient -> 'organization' -> 'encryptedIdentificationNumber' IS NULL;
         """, new RecipientTupleMapper())
    }


    int[] updateRecipientEncrypted(List<RecipientTuple> recipientsToEncrypt) {

        Map<String, String>[] params = recipientsToEncrypt.stream().map(
                recipient -> {
                    Map param = new HashMap<String, String>();
                    param.put("rowId", recipient.id)
                    param.put("organizationIdentificationNumber", encryptIfNotNull(recipient.organizationIdentificationNumber))
                    param.put("organizationTaxIdentificationNumber", encryptIfNotNull(recipient.organizationTaxIdentificationNumber))
                    param.put("billingAddressStreet1", encryptIfNotNull(recipient.billingAddressStreet1))
                    param.put("billingAddressStreet2", encryptIfNotNull(recipient.billingAddressStreet2))
                    return param
                }
        ).toList().toArray(Map[]::new)

        return namedJdbcTemplate.batchUpdate("""
            WITH 
            current_jsonb AS (
                select recipient as clear_recipient from invoice.document_request where id = :rowId::uuid),
            encrypted_jsonb AS (
                select jsonb_build_object(
                                'organization', jsonb_build_object(
                                'encryptedIdentificationNumber', :organizationIdentificationNumber::text,
                                'encryptedTaxIdentificationNumber', :organizationTaxIdentificationNumber::text
                            ),
                               'billingAddress', jsonb_build_object(
                                       'encryptedStreet1', :billingAddressStreet1::text,
                                       'encryptedStreet2', :billingAddressStreet2::text
                                   )
                           ) AS encrypted_recipient
            ) 
            
            UPDATE invoice.document_request
            SET recipient = invoice.jsonb_recursive_merge(clear_recipient_data, encrypted_recipient_data)
            FROM (select
                      (select clear_recipient from current_jsonb) as clear_recipient_data,
                      (select encrypted_recipient from encrypted_jsonb)  as encrypted_recipient_data
            ) AS data
            WHERE id = :rowId::uuid;
        """, params)
    }

    private String encryptIfNotNull(String s) {
        return s == null ? null : tenantCryptoService.encrypt(s);
    }

    private static class RecipientTuple {
        public String id;
        public String organizationIdentificationNumber;
        public String organizationTaxIdentificationNumber;
        public String billingAddressStreet1;
        public String billingAddressStreet2;

        RecipientTuple(
                String id,
                String organizationIdentificationNumber,
                String organizationTaxIdentificationNumber,
                String billingAddressStreet1,
                String billingAddressStreet2
        ) {
            this.id = id
            this.organizationIdentificationNumber = organizationIdentificationNumber
            this.organizationTaxIdentificationNumber = organizationTaxIdentificationNumber
            this.billingAddressStreet1 = billingAddressStreet1
            this.billingAddressStreet2 = billingAddressStreet2
        }
    }

    private static class RecipientTupleMapper implements RowMapper<RecipientTuple> {
        @Override
        RecipientTuple mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new RecipientTuple(
                    rs.getString("id"),
                    rs.getString("organization_identification_number"),
                    rs.getString("organization_tax_identification_number"),
                    rs.getString("billing_address_street1"),
                    rs.getString("billing_address_street2")
            )
        }
    }
}
