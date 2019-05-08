package pt.ulisboa.tecnico.softeng.bank.presentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import pt.ulisboa.tecnico.softeng.bank.exception.BankException;
import pt.ulisboa.tecnico.softeng.bank.services.local.BankInterface;
import pt.ulisboa.tecnico.softeng.bank.services.local.dataobjects.AccountData;
import pt.ulisboa.tecnico.softeng.bank.services.local.dataobjects.BankData;
import pt.ulisboa.tecnico.softeng.bank.services.local.dataobjects.BankOperationData;
import pt.ulisboa.tecnico.softeng.bank.services.local.dataobjects.ClientData;
import pt.ulisboa.tecnico.softeng.bank.services.remote.dataobjects.RestBankOperationData;

@Controller
@RequestMapping(value = "/banks/{code}/clients/{id}/accounts")
public class AccountController {
	private static Logger logger = LoggerFactory.getLogger(AccountController.class);

	@RequestMapping(method = RequestMethod.GET)
	public String accountForm(Model model, @PathVariable String code, @PathVariable String id) {
		logger.info("accountForm bankCode:{}, id:{}", code, id);

		ClientData clientData = BankInterface.getClientDataById(code, id);

		if (clientData == null) {
			model.addAttribute("error",
					"Error: it does not exist a client with id " + id + " in bank with code " + code);
			model.addAttribute("bank", new BankData());
			model.addAttribute("banks", BankInterface.getBanks());
			return "banks";
		} else {
			model.addAttribute("operation", new BankOperationData());
			model.addAttribute("client", clientData);
			return "accounts";
		}
	}

	@RequestMapping(method = RequestMethod.POST)
	public String accountSubmit(Model model, @PathVariable String code, @PathVariable String id) {
		logger.info("accountSubmit bankCode:{}, clientId:{}", code, id);

		try {
			BankInterface.createAccount(code, id);
		} catch (BankException be) {
			model.addAttribute("error", "Error: it was not possible to create de account");
			model.addAttribute("client", BankInterface.getClientDataById(code, id));
			return "accounts";
		}

		return "redirect:/banks/" + code + "/clients/" + id + "/accounts";
	}

	@RequestMapping(value = "/{iban}/operations", method = RequestMethod.GET)
	public String accountOperations(Model model, @PathVariable String code, @PathVariable String id,
			@PathVariable String iban) {
		logger.info("accountOperations bankCode:{}, clientId:{}, iban:{}", code, id, iban);

		ClientData client = BankInterface.getClientDataById(code, id);
		try {
			AccountData account = BankInterface.getAccountData(iban);
			model.addAttribute("operation", new BankOperationData());
			model.addAttribute("client", client);
			model.addAttribute("account", account);
			return "account";
		} catch (BankException be) {
			model.addAttribute("error", "Error: it was not possible to move to do the operations");
			model.addAttribute("client", BankInterface.getClientDataById(code, id));
			return "accounts";
		}

	}

	@RequestMapping(value = "/{iban}/deposit", method = RequestMethod.POST)
	public String accountDeposit(Model model, @PathVariable String code, @PathVariable String id,
			@PathVariable String iban, @ModelAttribute AccountData account) {
		logger.info("accountDeposit bankCode:{}, clientId:{}, iban:{}, amount:{}", code, id, iban, account.getAmount());

		try {
			BankInterface.deposit(iban, account.getAmount() != null ? account.getAmountLong() : -1);
			model.addAttribute("client", BankInterface.getClientDataById(code, id));
			model.addAttribute("account", BankInterface.getAccountData(iban));
			model.addAttribute("operation", new BankOperationData());
			return "redirect:/banks/" + code + "/clients/" + id + "/accounts/" + iban + "/operations";
		} catch (BankException be) {
			model.addAttribute("operation", new BankOperationData());
			model.addAttribute("error", "Error: it was not possible to execute the operation");
			model.addAttribute("client", BankInterface.getClientDataById(code, id));
			model.addAttribute("account", BankInterface.getAccountData(iban));
			return "account";
		}
	}

	@RequestMapping(value = "/{iban}/withdraw", method = RequestMethod.POST)
	public String accountWithdraw(Model model, @PathVariable String code, @PathVariable String id,
			@PathVariable String iban, @ModelAttribute AccountData account) {
		logger.info("accountWithdraw bankCode:{}, clientId:{}, iban:{}, amount:{}", code, id, iban,
				account.getAmount());

		try {
			BankInterface.withdraw(iban, account.getAmount() != null ? account.getAmountLong() : -1);
			model.addAttribute("client", BankInterface.getClientDataById(code, id));
			model.addAttribute("account", BankInterface.getAccountData(iban));
			model.addAttribute("operation", new BankOperationData());

			return "account";
		} catch (BankException be) {
			be.printStackTrace();
			model.addAttribute("error", "Error: it was not possible to execute the operation");
			model.addAttribute("client", BankInterface.getClientDataById(code, id));
			model.addAttribute("account", BankInterface.getAccountData(iban));
			model.addAttribute("operation", new BankOperationData());
			return "account";
		}

	}

	@RequestMapping(value = "/{iban}/transfer", method = RequestMethod.POST)
	public String accountTransfer(Model model, @PathVariable String code, @PathVariable String id,
			@PathVariable String iban, @ModelAttribute AccountData account,
			@ModelAttribute BankOperationData operation) {
				// (account.getIban(), "IBAN", value, transactionSource, transactionReference);
		
		logger.info("transfer sourceIban:{}, targetIban:{}, value:{}, transactionSource:{}, transactionReference:{}", iban, operation.getTargetIban(), 
				operation.getValueLong(), operation.getTransactionSource(), operation.getTransactionReference());



		ClientData client = BankInterface.getClientDataById(code, id);
		BankData bank = BankInterface.getBankDataByCode(client.getBankCode());
		
		// (account.getIban(), "IBAN", value, transactionSource, transactionReference);
		
		
		RestBankOperationData restOperation = new RestBankOperationData();
		restOperation.setReference(client.getId());		
		restOperation.setSourceIban(iban);
		restOperation.setTargetIban(operation.getTargetIban());
		restOperation.setValue(operation.getValueLong()*1000);
		restOperation.setTransactionReference(client.getBankCode() + bank.getOperations().size());
		restOperation.setTransactionSource("CLIENT" + client.getId());

		logger.info("transfer sourceIban:{}, targetIban:{}, value:{}, transactionSource:{}, transactionReference:{}", restOperation.getSourceIban(), restOperation.getTargetIban(), 
				restOperation.getValue(), restOperation.getTransactionSource(), restOperation.getTransactionReference());

		try {
			BankInterface.processPayment(restOperation);
			model.addAttribute("client", BankInterface.getClientDataById(code, id));
			model.addAttribute("account", BankInterface.getAccountData(iban));
			model.addAttribute("operation", new BankOperationData());
			return "account";
		} catch (BankException be) {
			model.addAttribute("error", "Error: it was not possible to execute the operation");
			model.addAttribute("client", BankInterface.getClientDataById(code, id));
			model.addAttribute("account", BankInterface.getAccountData(iban));
			model.addAttribute("operation", new BankOperationData());
			return "account";
		}

	}


}
