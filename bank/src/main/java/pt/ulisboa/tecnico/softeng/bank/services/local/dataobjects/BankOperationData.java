package pt.ulisboa.tecnico.softeng.bank.services.local.dataobjects;

import org.joda.time.DateTime;
import pt.ulisboa.tecnico.softeng.bank.domain.Bank;
import pt.ulisboa.tecnico.softeng.bank.domain.DepositOperation;
import pt.ulisboa.tecnico.softeng.bank.domain.WithdrawOperation;
import pt.ulisboa.tecnico.softeng.bank.domain.Operation;

public class BankOperationData implements Comparable<BankOperationData>{
    private String reference;
    private String type;
    private String sourceIban;
    private String targetIban;
    private Boolean formTransfer;
    private Double value;
    private DateTime time;
    private String transactionSource;
    private String transactionReference;
    private String cancellation;

    public BankOperationData() {
    }

    public BankOperationData(Operation operation) {
        this.reference = operation.getReference();
        this.cancellation = operation.getCancellation();
        this.type = operation.getType().name();
        this.sourceIban = operation.getSourceIban();
        this.targetIban = operation.getTargetIban();
        this.value = new Double(operation.getValue()) / Bank.SCALE;
        this.time = operation.getTime();
        this.transactionSource = operation.getTransactionSource();
        this.transactionReference = operation.getTransactionReference();

        switch (this.type) {
            case "DEPOSIT":
                this.formTransfer = ((DepositOperation)operation).getTransferOperationAsDeposit() != null;
                break;
            case "WITHDRAW":
                this.formTransfer = ((WithdrawOperation)operation).getTransferOperationAsWithdraw() != null;
                break;
            default:
                this.formTransfer = false;
                break;
        } 
        
    }

    public BankOperationData(String sourceIban, String targetIban, double value, String transactionSource, String transactionReference) {
        this.sourceIban = sourceIban;
        this.targetIban = targetIban;
        this.value = value;
        this.transactionSource = transactionSource;
        this.transactionReference = transactionReference;
    }

    public int compareTo(BankOperationData other) {
        return -this.getTime().compareTo(other.getTime());
    }

    public String getCancellation() {
        return this.cancellation;
    }

    public void setCancellation(String cancellation) {
        this.cancellation = cancellation;
    }


    public String getReference() {
        return this.reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceIban() {
        return this.sourceIban;
    }

    public void setSourceIban(String sourceIban) {
        this.sourceIban = sourceIban;
    }

    public String getTargetIban() {
        return this.targetIban;
    }

    public void setTargetIban(String targetIban) {
        this.targetIban = targetIban;
    }

    public Double getValue() {
        return this.value;
    }

    public long getValueLong() {
        return Math.round(this.value);
    }

    public void setValue(long value) {
        this.value = Long.valueOf(value).doubleValue() * Bank.SCALE;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public DateTime getTime() {
        return this.time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public String getTransactionSource() {
        return this.transactionSource;
    }

    public void setFromTransfer(Boolean value){
        this.formTransfer = value;
    }

    public Boolean getFromTransfer(){
        return this.formTransfer;
    }

    public void setTransactionSource(String transactionSource) {
        this.transactionSource = transactionSource;
    }

    public String getTransactionReference() {
        return this.transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

}
