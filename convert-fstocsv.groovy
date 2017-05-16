import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;

String query = "SELECT {pk} ,{isocode} FROM {Language as lang} WHERE {lang.active}=?isActive"

Map<String, Objects> queryParameters = new HashMap<String, String>();		
queryParameters.put("isActive",Boolean.TRUE);

FlexibleSearchQuery fsq = new FlexibleSearchQuery(query);
fsq.setResultClassList(Arrays.asList(String.class, String.class));
fsq.addQueryParameters(queryParameters);

List result = flexibleSearchService.search(fsq).getResult();		

StringBuilder sb = new StringBuilder()

//headers
sb.append("PK");
sb.append(";");
sb.append("iso Code");
sb.append(";");
sb.append("\n");

// Columns 
for(List lang : result )
{
	sb.append(lang.get(0));
	sb.append(";");
	sb.append(lang.get(1));
	sb.append(";")
	sb.append("\n");
}
sb.toString()