
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.bind.JAXBElement;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.chanel.hybris.fnb.ecom.data.FnbCCPaymentInfoData;
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy;
import com.chanel.hybris.fnb.ecom.data.PointOfSellEnum;
import com.chanel.hybris.fnb.enums.ContactModes;
import com.chanel.hybris.fnb.model.ContactModeModel;
import com.chanel.hybris.fnb.model.CustomerSegmentationModel;
import com.chanel.hybris.fnb.model.FnbVariantModel;
import com.chanel.hybris.fnb.services.FnbStockFacade;
import com.chanel.hybris.fnb.util.FnbUtils;
import com.chanel.hybris.fnb.ws.psp.client.facade.PspClientFacade;
import com.chanel.hybris.fnb.ws.psp.client.service.PspClientService;
import com.chanel.hybris.fnb.ws.psp.client.service.PspException;
import com.chanel.hybris.fnb.ws.psp.data.PaymentContext;
import com.chanel.hybris.fnb.ws.psp.data.PaymentResult;
import com.chanel.hybris.fnb.ws.psp.data.TransactionInitResult;
import com.chanel.hybris.fnb.ws.psp.generated.Address;
import com.chanel.hybris.fnb.ws.psp.generated.AddressOwner;
import com.chanel.hybris.fnb.ws.psp.generated.Buyer;
import com.chanel.hybris.fnb.ws.psp.generated.Card;
import com.chanel.hybris.fnb.ws.psp.generated.Cards;
import com.chanel.hybris.fnb.ws.psp.generated.Contract;
import com.chanel.hybris.fnb.ws.psp.generated.ContractNumberWalletList;
import com.chanel.hybris.fnb.ws.psp.generated.Details;
import com.chanel.hybris.fnb.ws.psp.generated.DisableWalletRequest;
import com.chanel.hybris.fnb.ws.psp.generated.DoCaptureRequest;
import com.chanel.hybris.fnb.ws.psp.generated.DoCaptureResponse;
import com.chanel.hybris.fnb.ws.psp.generated.DoRefundRequest;
import com.chanel.hybris.fnb.ws.psp.generated.DoRefundResponse;
import com.chanel.hybris.fnb.ws.psp.generated.DoResetRequest;
import com.chanel.hybris.fnb.ws.psp.generated.DoResetResponse;
import com.chanel.hybris.fnb.ws.psp.generated.DoWebPaymentRequest;
import com.chanel.hybris.fnb.ws.psp.generated.DoWebPaymentResponse;
import com.chanel.hybris.fnb.ws.psp.generated.GetAlertDetailsRequest;
import com.chanel.hybris.fnb.ws.psp.generated.GetAlertDetailsResponse;
import com.chanel.hybris.fnb.ws.psp.generated.GetCardsRequest;
import com.chanel.hybris.fnb.ws.psp.generated.GetCardsResponse;
import com.chanel.hybris.fnb.ws.psp.generated.GetMerchantSettingsRequest;
import com.chanel.hybris.fnb.ws.psp.generated.GetMerchantSettingsResponse;
import com.chanel.hybris.fnb.ws.psp.generated.GetTransactionDetailsRequest;
import com.chanel.hybris.fnb.ws.psp.generated.GetTransactionDetailsResponse;
import com.chanel.hybris.fnb.ws.psp.generated.GetWebPaymentDetailsRequest;
import com.chanel.hybris.fnb.ws.psp.generated.GetWebPaymentDetailsResponse;
import com.chanel.hybris.fnb.ws.psp.generated.ObjectFactory;
import com.chanel.hybris.fnb.ws.psp.generated.Order;
import com.chanel.hybris.fnb.ws.psp.generated.OrderDetail;
import com.chanel.hybris.fnb.ws.psp.generated.Owner;
import com.chanel.hybris.fnb.ws.psp.generated.Payment;
import com.chanel.hybris.fnb.ws.psp.generated.PointOfSell;
import com.chanel.hybris.fnb.ws.psp.generated.PrivateData;
import com.chanel.hybris.fnb.ws.psp.generated.PrivateDataList;
import com.chanel.hybris.fnb.ws.psp.generated.SelectedContractList;
import com.chanel.hybris.fnb.ws.psp.generated.UpdateWalletRequest;
import com.chanel.hybris.fnb.ws.psp.generated.Wallet;
import com.chanel.hybris.fnb.ws.psp.generated.WalletIdList;

import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.enums.SalesApplication;
import de.hybris.platform.core.enums.CreditCardType;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.order.OrderService;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.AddressService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;


		final String pointOfSellFO = getPointOfSell();
		final String pointOfSellBO = getPointOfSellBO();
		final String pointOfSellMO = getPointOfSellMO();

		final GetMerchantSettingsRequest request = new GetMerchantSettingsRequest();
		//final CatalogVersionModel catalogVersion = catalogVersionService.getCatalogVersion("fnbProductCatalog-ES","ES");
		final GetMerchantSettingsResponse merchantSettings = pspClientService.getMerchantSettings(request, "FR");
		BaseStoreModel baseStore = baseStoreService.getBaseStoreForUid("fnbwebsite-fr");
		
		final List<PointOfSell> pointOfSells = merchantSettings.getListPointOfSell().getPointOfSell();
		final Map<String, String> cardTypesContracts = new HashMap<String, String>();

		for (final PointOfSell pos : pointOfSells)
		{
			if (pointOfSellFO.equals(pos.getLabel()))
			{
				final List<Contract> contracts = pos.getContracts().getContract();
				int position = 0;
				for (final Contract contract : contracts)
				{
					String labelContract = "";
					final JAXBElement<String> label = contract.getLabel();
					if (label != null && label.getValue() != null)
					{
						labelContract = label.getValue();
					}
					// filtering 3DS contract
					if (!is3DS(labelContract))
					{
						position++;
						cardTypesContracts.put(PointOfSellEnum.FO.getCode() + contract.getCardType(),
								position + "|" + contract.getContractNumber());
					}
				}
			}
			if (pointOfSellMO.equals(pos.getLabel()))
			{
				final List<Contract> contracts = pos.getContracts().getContract();
				int position = 0;
				for (final Contract contract : contracts)
				{
					String labelContract = "";
					final JAXBElement<String> label = contract.getLabel();
					if (label != null && label.getValue() != null)
					{
						labelContract = label.getValue();
					}
					// filtering 3DS contract
					if (!is3DS(labelContract))
					{
						position++;
						cardTypesContracts.put(PointOfSellEnum.MO.getCode() + contract.getCardType(),
								position + "|" + contract.getContractNumber());
					}
				}
			}
			if (pointOfSellBO.equals(pos.getLabel()))
			{
				final List<Contract> contracts = pos.getContracts().getContract();
				int position = 0;
				for (final Contract contract : contracts)
				{
					String labelContract = "";
					final JAXBElement<String> label = contract.getLabel();
					if (label != null && label.getValue() != null)
					{
						labelContract = label.getValue();
					}

					// filtering 3DS contract
					if (!is3DS(labelContract))
					{
						position++;
						cardTypesContracts.put(PointOfSellEnum.BO.getCode() + contract.getCardType(),
								position + "|" + contract.getContractNumber());
					}
				}
			}
		}
		
		baseStore.setCardTypeContract(cardTypesContracts);
	    modelService.save(baseStore);
		
		
		/**
	 * gets the point of sell used in ecom site
	 *
	 * @return the point of sell used in ecom site
	 * @throws PspException
	 */
	public String getPointOfSell() throws PspException
	{
		final String key = "psp.point.sell.label";
		final String pos = configurationService.getConfiguration().getString(key);
		if (StringUtils.isEmpty(pos))
		{
			final String msg = "Missing point of sell for ecom site (desktop). Check key " + key;
			LOG.error(msg);
			throw new PspException(msg);
		}
		return pos;
	}

	/**
	 * gets the point of sell used in ecom site for payment by a callcenter agent
	 *
	 * @return the point of sell used in ecom site
	 * @throws PspException
	 */
	public String getPointOfSellBO() throws PspException
	{
		final String key = "psp.point.sell.bo.label";
		final String pos = configurationService.getConfiguration().getString(key);
		if (StringUtils.isEmpty(pos))
		{
			final String msg = "Missing point of sell for backoffice. Check key " + key;
			LOG.error(msg);
			throw new PspException(msg);
		}
		return pos;
	}


	/**
	 * gets the point of sell used in ecom site for payment by mobile
	 *
	 * @return the point of sell used in ecom site
	 * @throws PspException
	 */
	public String getPointOfSellMO() throws PspException
	{
		final String key = "psp.point.sell.mo.label";
		final String pos = configurationService.getConfiguration().getString(key);
		if (StringUtils.isEmpty(pos))
		{
			final String msg = "Missing point of sell for mobile. Check key " + key;
			LOG.error(msg);
			throw new PspException(msg);
		}
		return pos;
	}
		
		
     public boolean is3DS(final String labelContract)
	{
		return labelContract.toLowerCase().contains("3ds") || labelContract.toLowerCase().contains("safekey")|| labelContract.toLowerCase().contains("SK");
	}
			
			
	