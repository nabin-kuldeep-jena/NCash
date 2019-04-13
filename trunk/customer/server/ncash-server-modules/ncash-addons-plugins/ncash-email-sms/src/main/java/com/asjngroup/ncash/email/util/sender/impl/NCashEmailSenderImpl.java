package com.asjngroup.ncash.email.util.sender.impl;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.context.support.ResourceBundleMessageSource;

import com.asjngroup.ncash.email.util.EmailTemplateParam;
import com.asjngroup.ncash.email.util.NCashEmailException;
import com.asjngroup.ncash.email.util.sender.EmailSender;
import com.asjngroup.ncash.email.util.sender.EmailSendingFailedEception;
import com.asjngroup.ncash.email.util.sender.EmailTemplateType;

public class NCashEmailSenderImpl implements EmailSender
{

	private static final String EMAIL_HTML_TEMPLATE_MODE = "HTML5";

	private Properties javaMailProperties;

	public NCashEmailSenderImpl()
	{

	}
	
	

	public void sendPlainEmailMessage( List<String> toAddresses, List<String> ccAddresses, List<String> bccAddresses, String subject, String emailMessage ) throws EmailSendingFailedEception
	{
		try
		{
			NCashEmailHelper.sendMail( javaMailProperties, toAddresses, ccAddresses, bccAddresses, subject, emailMessage, null );
		}
		catch ( NCashEmailException e )
		{
			throw new EmailSendingFailedEception( e );
		}
	}

	public void sendEmailMessageWithAttachment( List<String> toAddresses, List<String> ccAddresses, List<String> bccAddresses, String subject, String emailMessage, List<File> attachedFiles ) throws EmailSendingFailedEception
	{
		try
		{
			NCashEmailHelper.sendMail( javaMailProperties, toAddresses, ccAddresses, bccAddresses, subject, emailMessage, null );
		}
		catch ( NCashEmailException e )
		{
			throw new EmailSendingFailedEception( e );
		}
	}

	public void sendHtmlTemplateEmailMessage( EmailTemplateParam emailTemplateParam ) throws EmailSendingFailedEception
	{
		final Context ctx = new Context( emailTemplateParam.getLocale() );
		HashMap<String, Object> templateParameters = emailTemplateParam.getTemplateParameters();
		if ( templateParameters != null )
		{
			for ( Entry<String, Object> templateValueEntry : templateParameters.entrySet() )
			{
				ctx.setVariable( templateValueEntry.getKey(), templateValueEntry.getValue() );
			}
		}

		try
		{
			// Prepare message using a Spring helper
			TemplateEngine templateEngine = getEmailTemplateEngine( emailTemplateParam.getEmailTemplateType() );
			String htmlContent = templateEngine.process( emailTemplateParam.getHtmlTemplateName(), ctx );
			NCashEmailHelper.sendHtmlMail( javaMailProperties, emailTemplateParam.getToAddresses(), emailTemplateParam.getCcAddresses(), emailTemplateParam.getBccAddresses(), emailTemplateParam.getSubject(), htmlContent, emailTemplateParam.getEmailImageParams() );
		}
		catch ( NCashEmailException e )
		{
			throw new EmailSendingFailedEception( e );
		}
	}

	protected TemplateEngine getEmailTemplateEngine( EmailTemplateType emailTemplateType )
	{
		final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		switch( emailTemplateType )
		{
		case TEXT:
			templateEngine.addTemplateResolver( textTemplateResolver() );
			break;
		case HTML:
			templateEngine.addTemplateResolver( htmlTemplateResolver() );
			break;
		case STRING:
			templateEngine.addTemplateResolver( stringTemplateResolver() );
			break;
		default:
			break;
		}
		templateEngine.setTemplateEngineMessageSource( emailMessageSource() );
		return templateEngine;
	}

	private ITemplateResolver textTemplateResolver()
	{
		final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
		templateResolver.setOrder( Integer.valueOf( 1 ) );
		templateResolver.setResolvablePatterns( Collections.singleton( "text/*" ) );
		templateResolver.setPrefix( "/WEB-INF/" );
		templateResolver.setSuffix( ".txt" );
		templateResolver.setTemplateMode( TemplateMode.TEXT );
		templateResolver.setCharacterEncoding( NCashEmailHelper.EMAIL_ENCODING );
		templateResolver.setCacheable( false );
		return templateResolver;
	}

	private ITemplateResolver htmlTemplateResolver()
	{
		final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
		templateResolver.setOrder( Integer.valueOf( 2 ) );
		templateResolver.setResolvablePatterns( Collections.singleton( "html/*" ) );
		templateResolver.setPrefix( "/WEB-INF/email/activation/" );
		templateResolver.setSuffix( ".html" );
		templateResolver.setTemplateMode( TemplateMode.HTML );
		templateResolver.setCharacterEncoding( NCashEmailHelper.EMAIL_ENCODING );
		templateResolver.setCacheable( false );
		return templateResolver;
	}

	private ITemplateResolver stringTemplateResolver()
	{
		final StringTemplateResolver templateResolver = new StringTemplateResolver();
		templateResolver.setOrder( Integer.valueOf( 3 ) );
		templateResolver.setTemplateMode( EMAIL_HTML_TEMPLATE_MODE );
		templateResolver.setCacheable( false );
		return templateResolver;
	}

	private ResourceBundleMessageSource emailMessageSource()
	{
		final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename( "email/template" );
		return messageSource;
	}

}
