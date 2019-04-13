package com.asjngroup.ncash.addons.razorpay.models;

public class RazorpayPaymentModel extends AbstractRazorpayModel
{
	private int amount;
	private String currency;
	private String status;
	private String method;
	private String order_id;
	private String description;
	private int amount_refunded;
	private String refund_status;
	private String email;
	private String contact;
	private int fee;
	private int service_tax;
	private String error_code;
	private String error_description;
	private String created_at;
	private RazorpayNoteModel notes;
	private String bank;
	private String wallet;
	private String vpa;

	public String getBank()
	{
		return bank;
	}

	public void setBank( String bank )
	{
		this.bank = bank;
	}

	public String getWallet()
	{
		return wallet;
	}

	public void setWallet( String wallet )
	{
		this.wallet = wallet;
	}

	public String getVpa()
	{
		return vpa;
	}

	public void setVpa( String vpa )
	{
		this.vpa = vpa;
	}

	public int getAmount()
	{
		return amount;
	}

	public void setAmount( int amount )
	{
		this.amount = amount;
	}

	public String getCurrency()
	{
		return currency;
	}

	public void setCurrency( String currency )
	{
		this.currency = currency;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus( String status )
	{
		this.status = status;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod( String method )
	{
		this.method = method;
	}

	public String getOrder_id()
	{
		return order_id;
	}

	public void setOrder_id( String order_id )
	{
		this.order_id = order_id;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription( String description )
	{
		this.description = description;
	}

	public int getAmount_refunded()
	{
		return amount_refunded;
	}

	public void setAmount_refunded( int amount_refunded )
	{
		this.amount_refunded = amount_refunded;
	}

	public String getRefund_status()
	{
		return refund_status;
	}

	public void setRefund_status( String refund_status )
	{
		this.refund_status = refund_status;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail( String email )
	{
		this.email = email;
	}

	public String getContact()
	{
		return contact;
	}

	public void setContact( String contact )
	{
		this.contact = contact;
	}

	public int getFee()
	{
		return fee;
	}

	public void setFee( int fee )
	{
		this.fee = fee;
	}

	public int getService_tax()
	{
		return service_tax;
	}

	public void setService_tax( int service_tax )
	{
		this.service_tax = service_tax;
	}

	public String getError_code()
	{
		return error_code;
	}

	public void setError_code( String error_code )
	{
		this.error_code = error_code;
	}

	public String getError_description()
	{
		return error_description;
	}

	public void setError_description( String error_description )
	{
		this.error_description = error_description;
	}

	public String getCreated_at()
	{
		return created_at;
	}

	public void setCreated_at( String created_at )
	{
		this.created_at = created_at;
	}

	public RazorpayNoteModel getNotes()
	{
		return notes;
	}

	public void setNotes( RazorpayNoteModel notes )
	{
		this.notes = notes;
	}

}
