<alias name="brakeSolrQueryPostProcessors" alias="solrQueryPostProcessors" />
	<util:list id="brakeSolrQueryPostProcessors">
		<bean id="brakeSolrQueryPostProcessor" class="fr.marcopolo.core.search.BrakeSolrQueryPostProcessor">
			<property name="fieldNameProvider" ref="solrFieldNameProvider" />
			<property name="userService" ref="userService" />
			<property name="sessionService" ref="sessionService"/>
		</bean>
	</util:list>
package fr.marcopolo.core.search;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.search.QueryField;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SolrQueryPostProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Joiner;

import fr.marcopolo.core.constants.BrakeConstant;
import fr.marcopolo.core.model.BrakeB2BCustomerModel;
import fr.marcopolo.core.model.BrakeProductModel;
import fr.marcopolo.core.user.BrakeUserService;


/**
 * post processor to add sort
 * 
 * RM :
 * 
 * @author aelchgar
 */
public class BrakeSolrQueryPostProcessor implements SolrQueryPostProcessor
{
	/**
	 * 
	 */
	private static final String CHAR_E_COMMERCIAL = " & ";
	private FieldNameProvider fieldNameProvider;
	private SessionService sessionService;
	private BrakeUserService userService;

	@Override
	public SolrQuery process(final SolrQuery solrQuery, final SearchQuery query)
	{

		if (BrakeProductModel._TYPECODE.equals(query.getIndexedType().getCode()))
		{
			addEligibilityFilter(solrQuery, query);
			addDivisionFilter(solrQuery, query);
			addCustomerRestriction(solrQuery, query);

			final Boolean promotionChecked = sessionService.getAttribute("promotionChecked");
			if (promotionChecked != null && promotionChecked)
			{
				addPromotionsFilter(solrQuery, query);
			}

			final Boolean mercurialeChecked = sessionService.getAttribute("MercurialeChecked");
			if (mercurialeChecked != null && mercurialeChecked)
			{
				addMercurialeFilter(solrQuery, query);
			}

		}

		return solrQuery;
	}

	private void addCustomerRestriction(final SolrQuery solrQuery, final SearchQuery query)
	{
		final UserModel user = userService.getCurrentUser();
		final IndexedProperty accessForbiddenForAllCustomerProprtie = query.getIndexedType().getIndexedProperties()
				.get("accessForbiddenForAllCustomer");
		final IndexedProperty codeGammeProperty = query.getIndexedType().getIndexedProperties().get("codeGamme");
		final IndexedProperty codeWithZerosProperty = query.getIndexedType().getIndexedProperties().get("codeWithZeros");
		final IndexedProperty nameProperty = query.getIndexedType().getIndexedProperties().get("name");

		final String accessForbiddenForAllCustomerName = fieldNameProvider.getFieldName(accessForbiddenForAllCustomerProprtie,
				null, FieldNameProvider.FieldType.INDEX);
		final String codeGammeName = fieldNameProvider.getFieldName(codeGammeProperty, null, FieldNameProvider.FieldType.INDEX);
		final String codeWithZeros = fieldNameProvider.getFieldName(codeWithZerosProperty, null, FieldNameProvider.FieldType.INDEX);
		final String name = fieldNameProvider.getFieldName(nameProperty, null, FieldNameProvider.FieldType.INDEX);

		solrQuery.addFilterQuery(accessForbiddenForAllCustomerName + ":false");

		if (!userService.isAnonymousUser(user) && user instanceof BrakeB2BCustomerModel)
		{
			final StringBuffer customerRestriction = new StringBuffer();
			final Map<String, List<String>> mapMercuriale = sessionService.getAttribute(BrakeConstant.MERCURIALE_MAP);
			if (mapMercuriale != null)
			{
				final List<String> codeProductsCodeGamme = mapMercuriale.get(BrakeConstant.MERCURIALE_NUM_PRODUCT_CODE_GAMME);

				//RG- afficher que les produits PSN-PSR-PNG qui sont dans la mercurial client
				if (CollectionUtils.isNotEmpty(codeProductsCodeGamme))
				{
					customerRestriction.append("(");
					customerRestriction.append("(");
					customerRestriction.append(" *:* NOT ").append(
							codeGammeName + ":(" + BrakeConstant.PRODUCT_NOT_ACCES_IN_MODE_CONNECTE + ")");
					customerRestriction.append(")");
					customerRestriction.append(" OR ");
					customerRestriction.append("(");
					customerRestriction.append(codeGammeName + ":(" + BrakeConstant.PRODUCT_NOT_ACCES_IN_MODE_CONNECTE + ")");
					customerRestriction.append(" AND ");
					final StringBuilder sb = new StringBuilder();
					Joiner.on(CHAR_E_COMMERCIAL).appendTo(sb, codeProductsCodeGamme);
					customerRestriction.append(codeWithZeros).append(":(").append(sb.toString()).append(")");
					customerRestriction.append(")");
					customerRestriction.append(" OR ");
					customerRestriction.append("(");
					customerRestriction.append(" *:* NOT ").append(name).append(":['' TO *]");
					customerRestriction.append(")");
					customerRestriction.append(")");
					solrQuery.addFilterQuery(customerRestriction.toString());
				}
				else
				{
					final StringBuilder codeGammeRestrcition = new StringBuilder();
					codeGammeRestrcition.append("(");
					codeGammeRestrcition.append("(");
					codeGammeRestrcition.append(" *:* NOT ").append(
							codeGammeName + ":(" + BrakeConstant.PRODUCT_NOT_ACCES_IN_MODE_CONNECTE + ")");
					codeGammeRestrcition.append(")");
					codeGammeRestrcition.append(" OR ");
					codeGammeRestrcition.append("(");
					codeGammeRestrcition.append(" *:* NOT ").append(name).append(":['' TO *]");
					codeGammeRestrcition.append(")");
					codeGammeRestrcition.append(")");
					solrQuery.addFilterQuery(codeGammeRestrcition.toString());
				}
			}
			else
			{
				final StringBuilder codeGammeRestrcition = new StringBuilder();
				codeGammeRestrcition.append("(");
				codeGammeRestrcition.append("(");
				codeGammeRestrcition.append(" *:* NOT ").append(
						codeGammeName + ":(" + BrakeConstant.PRODUCT_NOT_ACCES_IN_MODE_CONNECTE + ")");
				codeGammeRestrcition.append(")");
				codeGammeRestrcition.append(" OR ");
				codeGammeRestrcition.append("(");
				codeGammeRestrcition.append(" *:* NOT ").append(name).append(":['' TO *]");
				codeGammeRestrcition.append(")");
				codeGammeRestrcition.append(")");
				solrQuery.addFilterQuery(codeGammeRestrcition.toString());
			}
		}
		else
		{
			final StringBuilder codeGammeRestrcition = new StringBuilder();
			codeGammeRestrcition.append("(");
			codeGammeRestrcition.append("(");
			codeGammeRestrcition.append(" *:* NOT ").append(
					codeGammeName + ":(" + BrakeConstant.PRODUCT_NOT_ACCES_IN_MODE_CONNECTE + ")");
			codeGammeRestrcition.append(")");
			codeGammeRestrcition.append(" OR ");
			codeGammeRestrcition.append("(");
			codeGammeRestrcition.append(" *:* NOT ").append(name).append(":['' TO *]");
			codeGammeRestrcition.append(")");
			codeGammeRestrcition.append(")");
			solrQuery.addFilterQuery(codeGammeRestrcition.toString());
		}
	}

	/**
	 * @param solrQuery
	 * @param query
	 */
	private void addDivisionFilter(final SolrQuery solrQuery, final SearchQuery query)
	{
		final UserModel user = userService.getCurrentUser();
		if (!userService.isAnonymousUser(user) && user instanceof BrakeB2BCustomerModel)
		{
			final IndexedProperty divisionsProperty = query.getIndexedType().getIndexedProperties().get("divisions");
			final String divisionsName = fieldNameProvider.getFieldName(divisionsProperty, null, FieldNameProvider.FieldType.INDEX);
			final BrakeB2BCustomerModel customer = (BrakeB2BCustomerModel) user;
			final String customerDivision = customer.getDivisionLogistiqueRM();
			solrQuery.addFilterQuery(divisionsName + ":(" + customerDivision + ")");
		}

	}

	/**
	 * @param solrQuery
	 * @param query
	 */
	private void addEligibilityFilter(final SolrQuery solrQuery, final SearchQuery query)
	{
		if (!isClearanceOffreCategory(query))
		{
			final IndexedProperty eligibleForClearanceProperty = query.getIndexedType().getIndexedProperties()
					.get("eligibleForClearance");

			final String eligibleForClearanceName = fieldNameProvider.getFieldName(eligibleForClearanceProperty, null,
					FieldNameProvider.FieldType.INDEX);
			solrQuery.addFilterQuery(eligibleForClearanceName + ":false");
		}
	}

	private Boolean isClearanceOffreCategory(final SearchQuery query)
	{
		final List<QueryField> queryFields = query.getAllFields();
		if (CollectionUtils.isNotEmpty(queryFields))
		{
			for (final QueryField queryField : queryFields)
			{
				if (BrakeConstant.CATEGORY_ALLCATEGORIES_CODE.equals(queryField.getField())
						&& CollectionUtils.isNotEmpty(queryField.getValues()))
				{
					for (final String queryValue : queryField.getValues())
					{
						if (BrakeConstant.CATEGORY_OFFRESASAISIR_CODE.equalsIgnoreCase(queryValue))
						{
							return Boolean.TRUE;
						}
					}
				}
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * @param solrQuery
	 * @param query
	 */
	private void addPromotionsFilter(final SolrQuery solrQuery, final SearchQuery query)
	{
		final List<String> mapPromotion = sessionService.getAttribute(BrakeConstant.PROMOTION_PRODUCT_CODE_MAP);
		final UserModel customer = userService.getCurrentUser();
		if (!userService.isAnonymousUser(customer) && customer instanceof BrakeB2BCustomerModel
				&& CollectionUtils.isNotEmpty(mapPromotion))
		{
			final IndexedProperty codeWithZerosProperty = query.getIndexedType().getIndexedProperties().get("codeWithZeros");
			final String codeWithZerosName = fieldNameProvider.getFieldName(codeWithZerosProperty, null,
					FieldNameProvider.FieldType.INDEX);
			final StringBuilder promotionFiltreBuilder = new StringBuilder();
			final StringBuilder sb = new StringBuilder();
			Joiner.on(CHAR_E_COMMERCIAL).appendTo(sb, mapPromotion);
			promotionFiltreBuilder.append(codeWithZerosName).append(":(").append(sb.toString()).append(")");
			solrQuery.addFilterQuery(promotionFiltreBuilder.toString());
		}
	}

	private Map<String, String> mercurialeFilter(final SearchQuery query)
	{
		final Map<String, List<String>> mapMercuriale = sessionService.getAttribute(BrakeConstant.MERCURIALE_MAP);
		final Map<String, String> mercurial = new HashMap<String, String>();
		final UserModel user = userService.getCurrentUser();
		if (!userService.isAnonymousUser(user) && user instanceof BrakeB2BCustomerModel && mapMercuriale != null)
		{

			final IndexedProperty codeWithZerosProperty = query.getIndexedType().getIndexedProperties().get("codeWithZeros");
			final IndexedProperty codeQualificatifProperty = query.getIndexedType().getIndexedProperties().get("codeQualificatif");
			final IndexedProperty codeCategorieProperty = query.getIndexedType().getIndexedProperties().get("codeCategorie");
			final IndexedProperty codeRegroupementProperty = query.getIndexedType().getIndexedProperties().get("codeRegroupement");
			final IndexedProperty codeFamilleProperty = query.getIndexedType().getIndexedProperties().get("codeFamille");
			final IndexedProperty codeSousFamilleProperty = query.getIndexedType().getIndexedProperties().get("codeSousFamille");


			final String codeWithZeros = fieldNameProvider.getFieldName(codeWithZerosProperty, null,
					FieldNameProvider.FieldType.INDEX);
			final String codeQualificatif = fieldNameProvider.getFieldName(codeQualificatifProperty, null,
					FieldNameProvider.FieldType.INDEX);
			final String codeCategorie = fieldNameProvider.getFieldName(codeCategorieProperty, null,
					FieldNameProvider.FieldType.INDEX);
			final String codeRegroupement = fieldNameProvider.getFieldName(codeRegroupementProperty, null,
					FieldNameProvider.FieldType.INDEX);
			final String codeFamille = fieldNameProvider.getFieldName(codeFamilleProperty, null, FieldNameProvider.FieldType.INDEX);
			final String codeSousFamille = fieldNameProvider.getFieldName(codeSousFamilleProperty, null,
					FieldNameProvider.FieldType.INDEX);


			final List<String> numsProduct = mapMercuriale.get(BrakeConstant.MERCURIALE_NUM_PRODUCT);

			if (CollectionUtils.isNotEmpty(numsProduct))
			{
				final StringBuilder codeProductBuilder = new StringBuilder();

				Joiner.on(CHAR_E_COMMERCIAL).appendTo(codeProductBuilder, numsProduct);

				mercurial.put(codeWithZeros, ":(" + codeProductBuilder.toString() + ")");
			}
			//

			final List<String> codesQualif = mapMercuriale.get(BrakeConstant.MERCURIALE_CODE_QUALIFICATIF);

			if (CollectionUtils.isNotEmpty(codesQualif))
			{
				final StringBuilder codeQualificatifBuilder = new StringBuilder();

				Joiner.on(CHAR_E_COMMERCIAL).appendTo(codeQualificatifBuilder, codesQualif);

				mercurial.put(codeQualificatif, ":(" + codeQualificatifBuilder.toString() + ")");
			}
			//

			final List<String> codesCat = mapMercuriale.get(BrakeConstant.MERCURIALE_CODE_CATEGORIE);

			if (CollectionUtils.isNotEmpty(codesCat))
			{
				final StringBuilder codeCategorieBuilder = new StringBuilder();

				Joiner.on(CHAR_E_COMMERCIAL).appendTo(codeCategorieBuilder, codesCat);
				mercurial.put(codeCategorie, ":(" + codeCategorieBuilder.toString() + ")");
			}
			//
			final List<String> codesReg = mapMercuriale.get(BrakeConstant.MERCURIALE_CODE_GROUPE);
			if (CollectionUtils.isNotEmpty(codesReg))
			{
				final StringBuilder codeRegroupementBuilder = new StringBuilder();

				Joiner.on(CHAR_E_COMMERCIAL).appendTo(codeRegroupementBuilder, codesReg);
				mercurial.put(codeRegroupement, ":(" + codeRegroupementBuilder.toString() + ")");
			}
			//
			final List<String> codesFam = mapMercuriale.get(BrakeConstant.MERCURIALE_CODE_FAMILLE);

			if (CollectionUtils.isNotEmpty(codesFam))
			{
				final StringBuilder codeFamilleBuilder = new StringBuilder();

				Joiner.on(CHAR_E_COMMERCIAL).appendTo(codeFamilleBuilder, codesFam);
				mercurial.put(codeFamille, ":(" + codeFamilleBuilder.toString() + ")");
			}
			//
			final List<String> codesSousFam = mapMercuriale.get(BrakeConstant.MERCURIALE_CODE_SOUS_FAMILLE);

			if (CollectionUtils.isNotEmpty(codesSousFam))
			{
				final StringBuilder codeSousFamilleBuilder = new StringBuilder();

				Joiner.on(CHAR_E_COMMERCIAL).appendTo(codeSousFamilleBuilder, codesSousFam);
				mercurial.put(codeSousFamille, ":(" + codeSousFamilleBuilder.toString() + ")");
			}
		}

		return mercurial;

	}

	private void addMercurialeFilter(final SolrQuery solrQuery, final SearchQuery query)
	{
		final Map<String, String> mapMercuriale = mercurialeFilter(query);

		final IndexedProperty codeWithZerosProperty = query.getIndexedType().getIndexedProperties().get("codeWithZeros");
		final IndexedProperty codeQualificatifProperty = query.getIndexedType().getIndexedProperties().get("codeQualificatif");
		final IndexedProperty codeCategorieProperty = query.getIndexedType().getIndexedProperties().get("codeCategorie");
		final IndexedProperty codeRegroupementProperty = query.getIndexedType().getIndexedProperties().get("codeRegroupement");
		final IndexedProperty codeFamilleProperty = query.getIndexedType().getIndexedProperties().get("codeFamille");
		final IndexedProperty codeSousFamilleProperty = query.getIndexedType().getIndexedProperties().get("codeSousFamille");


		final String codeWithZeros = fieldNameProvider.getFieldName(codeWithZerosProperty, null, FieldNameProvider.FieldType.INDEX);
		final String codeQualificatif = fieldNameProvider.getFieldName(codeQualificatifProperty, null,
				FieldNameProvider.FieldType.INDEX);
		final String codeCategorie = fieldNameProvider.getFieldName(codeCategorieProperty, null, FieldNameProvider.FieldType.INDEX);
		final String codeRegroupement = fieldNameProvider.getFieldName(codeRegroupementProperty, null,
				FieldNameProvider.FieldType.INDEX);
		final String codeFamille = fieldNameProvider.getFieldName(codeFamilleProperty, null, FieldNameProvider.FieldType.INDEX);
		final String codeSousFamille = fieldNameProvider.getFieldName(codeSousFamilleProperty, null,
				FieldNameProvider.FieldType.INDEX);



		if (mapMercuriale.get(codeWithZeros) != null)
		{
			solrQuery.addFilterQuery(codeWithZeros + mapMercuriale.get(codeWithZeros));
		}
		//
		if (mapMercuriale.get(codeQualificatif) != null)
		{
			solrQuery.addFilterQuery(codeQualificatif + mapMercuriale.get(codeQualificatif));
		}
		//
		if (mapMercuriale.get(codeCategorie) != null)
		{
			solrQuery.addFilterQuery(codeCategorie + mapMercuriale.get(codeCategorie));
		}
		//
		if (mapMercuriale.get(codeRegroupement) != null)
		{
			solrQuery.addFilterQuery(codeRegroupement + mapMercuriale.get(codeRegroupement));
		}
		//
		if (mapMercuriale.get(codeFamille) != null)
		{
			solrQuery.addFilterQuery(codeFamille + mapMercuriale.get(codeFamille));
		}
		//
		if (mapMercuriale.get(codeSousFamille) != null)
		{
			solrQuery.addFilterQuery(codeSousFamille + mapMercuriale.get(codeSousFamille));
		}

	}

	public FieldNameProvider getFieldNameProvider()
	{
		return fieldNameProvider;
	}

	/**
	 * Setter for the fieldNameProvider.
	 * 
	 * @param fieldNameProvider
	 *           The fieldNameProvider to set
	 */
	@Required
	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}

	/**
	 * @param userService
	 *           the userService to set
	 */
	public void setUserService(final BrakeUserService userService)
	{
		this.userService = userService;
	}

	/**
	 * @return the sessionService
	 */
	public SessionService getSessionService()
	{
		return sessionService;
	}

	/**
	 * @param sessionService
	 *           the sessionService to set
	 */
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}



}
