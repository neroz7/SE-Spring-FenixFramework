package pt.ulisboa.tecnico.softeng.tax.presentation;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import antlr.collections.List;
import pt.ulisboa.tecnico.softeng.tax.domain.TaxPayer;
import pt.ulisboa.tecnico.softeng.tax.exception.TaxException;
import pt.ulisboa.tecnico.softeng.tax.services.local.TaxInterface;
import pt.ulisboa.tecnico.softeng.tax.services.local.dataobjects.InvoiceData;
import pt.ulisboa.tecnico.softeng.tax.services.local.dataobjects.TaxPayerData;

//http://localhost:8086/tax/taxes/2018

@Controller
@RequestMapping(value = "/tax/taxes/{year}")
public class ShowTaxesController {
	private static Logger logger = LoggerFactory.getLogger(ShowTaxesController.class);

	@RequestMapping(method = RequestMethod.GET)
	public String invoiceForm(Model model, @PathVariable Integer year) {
        logger.info("ShowTaxes year:{}", year);
        ArrayList<TaxPayerData> payers = new ArrayList<TaxPayerData>(TaxInterface.getTaxPayerDataList());
        ArrayList<InvoiceData> invoices = new ArrayList<InvoiceData>();
        Double returns = 0.0;

        for (TaxPayerData p : payers) {
            if(p.getReturns() != null && p.getReturns().get(year) != null)
                returns += p.getReturns().get(year);

            for(InvoiceData invoice : TaxInterface.getInvoiceDataList(p.getNif()))
                if(invoice.getDate().year().get() == year)    
                    invoices.add(invoice);
        }
        
        model.addAttribute("invoices", new Integer(year));
        model.addAttribute("invoices", invoices);
		model.addAttribute("returns", returns);
		return "showTaxesView";
	}
}
