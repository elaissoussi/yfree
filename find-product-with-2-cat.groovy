import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import com.chanel.pcm.core.model.FshProductVariantModel
import de.hybris.platform.category.model.CategoryModel; 


def query = "Select {pr.pk} from { FshProductVariant as pr join CategoryProductRelation AS rel on {pr.pk}={rel.target} join Category as cat on {cat.pk} = {rel.source}} WHERE {cat.code} in ('FshTag_ES18A_PIMTAGCATEGORY','FshTag_ES18B_PIMTAGCATEGORY','FshTag_ES18C_PIMTAGCATEGORY','FshTag_ES18P_PIMTAGCATEGORY','FshTag_ES1BS_PIMTAGCATEGORY')"

FlexibleSearchQuery fsq = new FlexibleSearchQuery(query);

List products = flexibleSearchService.search(fsq).getResult();

for(FshProductVariantModel fshpv : products )
{
	List superCategories = fshpv.getSupercategories();
	
  	def cats = []
  
	for(CategoryModel cat : superCategories)
	    {
          	
			if(cat.getCode().startsWith("TargetDiffusion"))
			{
				cats.add(cat.getCode());
			}
		}
    if(cats.size() == 1 && cats[0].equals("TargetDiffusion_BOUTIQUEAPP_nomarket")
       {
       		println fshpv.getCode()
       }
}
