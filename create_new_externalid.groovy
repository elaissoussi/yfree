def query = "SELECT {c.pk} FROM  {Customer as c}  GROUP BY {c.externalId} HAVING COUNT(*) > 1"

def result = flexibleSearchService.search(query).getResult()	


result.each 
{
	print it.getUid()
  
	def newExternalId = customerExternalIdGenerator.generate()
	
	it.setDefaultNeedRanking(EfoodNeedRankEnum.BY_NEED_PRICE)
	
        println " - externalId : " + newExternalId 
  
	it.setExternalId(newExternalId)
  
	modelService.save(it)
}
