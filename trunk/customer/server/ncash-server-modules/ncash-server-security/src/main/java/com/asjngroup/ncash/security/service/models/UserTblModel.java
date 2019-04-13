package com.asjngroup.ncash.security.service.models;

import com.asjngroup.ncash.common.service.models.AbstractResponseModel;

public class UserTblModel extends AbstractResponseModel
{

	private String usrForename = null;
	private String usrSurname = null;
	private String usrEmailAddress = null;
	private String usrMobNo = null;
	//private String usrCountryNoCode = null;
	private Integer usrCtyId = null;
	private Integer usrSteId = null;
	private Boolean usrEmailVerifyFl;
	
	public Integer getUsrCtyId()
	{
		return usrCtyId;
	}

	public void setUsrCtyId( Integer usrCtyId )
	{
		this.usrCtyId = usrCtyId;
	}

	public Integer getUsrSteId()
	{
		return usrSteId;
	}

	public void setUsrSteId( Integer usrSteId )
	{
		this.usrSteId = usrSteId;
	}

	public String getUsrForename()
	{
		return usrForename;
	}

	public void setUsrForename( String usrForename )
	{
		this.usrForename = usrForename;
	}

	public String getUsrSurname()
	{
		return usrSurname;
	}

	public void setUsrSurname( String usrSurname )
	{
		this.usrSurname = usrSurname;
	}

	public String getUsrEmailAddress()
	{
		return usrEmailAddress;
	}

	public void setUsrEmailAddress( String usrEmailAddress )
	{
		this.usrEmailAddress = usrEmailAddress;
	}

	public String getUsrMobNo()
	{
		return usrMobNo;
	}

	public void setUsrMobNo( String usrMobNo )
	{
		this.usrMobNo = usrMobNo;
	}

	public Boolean getUsrEmailVerifyFl()
	{
		return usrEmailVerifyFl;
	}

	public void setUsrEmailVerifyFl(Boolean usrEmailVerifyFl) 
	{
		this.usrEmailVerifyFl = usrEmailVerifyFl;
	}

}
