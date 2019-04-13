package com.asjngroup.ncash.common.models;

public class RegisterUserModel
{
	private String urtName = null;
	private String urtPassword = null;
	private String urtForename = null;
	private String urtSurname = null;
	private String urtEmailAddress = null;
	private String urtMobNo = null;
	private String urtDescription = null;
	//private String urtCountryNoCode = null;
	private String uidToken = null;
	private int ctyId;

	public String getUrtName()
	{
		return urtName;
	}

	public void setUrtName( String urtName )
	{
		this.urtName = urtName;
	}

	public String getUrtPassword()
	{
		return urtPassword;
	}

	public void setUrtPassword( String urtPassword )
	{
		this.urtPassword = urtPassword;
	}

	public String getUrtForename()
	{
		return urtForename;
	}

	public void setUrtForename( String urtForename )
	{
		this.urtForename = urtForename;
	}

	public String getUrtSurname()
	{
		return urtSurname;
	}

	public void setUrtSurname( String urtSurname )
	{
		this.urtSurname = urtSurname;
	}

	public String getUrtEmailAddress()
	{
		return urtEmailAddress;
	}

	public void setUrtEmailAddress( String urtEmailAddress )
	{
		this.urtEmailAddress = urtEmailAddress;
	}

	public String getUrtMobNo()
	{
		return urtMobNo;
	}

	public void setUrtMobNo( String urtMobNo )
	{
		this.urtMobNo = urtMobNo;
	}

	public String getUrtDescription()
	{
		return urtDescription;
	}

	public void setUrtDescription( String urtDescription )
	{
		this.urtDescription = urtDescription;
	}

//	public String getUrtCountryNoCode()
//	{
//		return urtCountryNoCode;
//	}
//
//	public void setUrtCountryNoCode( String urtCountryNoCode )
//	{
//		this.urtCountryNoCode = urtCountryNoCode;
//	}

	public String getUidToken()
	{
		return uidToken;
	}

	public void setUidToken( String uidToken )
	{
		this.uidToken = uidToken;
	}

	public int getCtyId() {
		return ctyId;
	}

	public void setCtyId(int ctyId) {
		this.ctyId = ctyId;
	}

}
