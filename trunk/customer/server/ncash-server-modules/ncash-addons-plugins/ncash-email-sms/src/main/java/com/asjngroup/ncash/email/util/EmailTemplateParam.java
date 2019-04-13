package com.asjngroup.ncash.email.util;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.asjngroup.ncash.email.util.sender.EmailTemplateType;

public class EmailTemplateParam
{
	private String subject;
	private EmailTemplateType emailTemplateType;
	private String imageContentType;
	private Locale locale;
	private HashMap<String, Object> templateParameters;
	private String htmlTemplateName;
	private List<EmailImageParam> emailImageParams;
	List<String> toAddresses;
	List<String> ccAddresses;
	List<String> bccAddresses;
	

	public List<String> getToAddresses()
	{
		return toAddresses;
	}

	public void setToAddresses( List<String> toAddresses )
	{
		this.toAddresses = toAddresses;
	}

	public List<String> getCcAddresses()
	{
		return ccAddresses;
	}

	public void setCcAddresses( List<String> ccAddresses )
	{
		this.ccAddresses = ccAddresses;
	}

	public List<String> getBccAddresses()
	{
		return bccAddresses;
	}

	public void setBccAddresses( List<String> bccAddresses )
	{
		this.bccAddresses = bccAddresses;
	}

	public List<EmailImageParam> getEmailImageParams()
	{
		return emailImageParams;
	}

	public void setEmailImageParams( List<EmailImageParam> emailImageParams )
	{
		this.emailImageParams = emailImageParams;
	}

	public String getHtmlTemplateName()
	{
		return htmlTemplateName;
	}

	public void setHtmlTemplateName( String htmlTemplateName )
	{
		this.htmlTemplateName = htmlTemplateName;
	}

	public String getSubject()
	{
		return subject;
	}

	public void setSubject( String subject )
	{
		this.subject = subject;
	}

	public EmailTemplateType getEmailTemplateType()
	{
		return emailTemplateType;
	}

	public void setEmailTemplateType( EmailTemplateType emailTemplateType )
	{
		this.emailTemplateType = emailTemplateType;
	}

	public String getImageContentType()
	{
		return imageContentType;
	}

	public void setImageContentType( String imageContentType )
	{
		this.imageContentType = imageContentType;
	}

	public Locale getLocale()
	{
		return locale;
	}

	public void setLocale( Locale locale )
	{
		this.locale = locale;
	}

	public HashMap<String, Object> getTemplateParameters()
	{
		return templateParameters;
	}

	public void setTemplateParameters( HashMap<String, Object> templateParameters )
	{
		this.templateParameters = templateParameters;
	}

}
