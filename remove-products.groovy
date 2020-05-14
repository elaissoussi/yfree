def query = "****";
	
def fsq = new FlexibleSearchQuery(query)
fsq.setCount(200)

// iteration number to determine using select count(pk)
for (int i = 0 ; i <= iterationNumer ; i++ )
{

println "start iteration " + i

def timeSlots = flexibleSearchService.search(fsq).getResult()


Transaction tx = Transaction.current()
tx.begin()

boolean success = false;
println "Time slot size " + timeSlots.size()
try 
{

	modelService.removeAll(timeSlots);
	success = true;
}
catch (final Exception exception) 
{

	println "Exception : {} ",exception.getMessage();
}
finally
{
	if(success) 
        {
		tx.commit();
	}
	else 
        {
		tx.rollback();
	}
}

println "End iteration " + i+" success = "+success
}
