package com.asjngroup.ncash.email.util.sender.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.asjngroup.ncash.common.util.StringHelper;
import com.asjngroup.ncash.email.util.EmailImageParam;
import com.asjngroup.ncash.email.util.NCashEmailException;
import com.asjngroup.ncash.email.util.NcashMailAuthenticator;

public class NCashEmailHelper
{
	private static final Log log = LogFactory.getLog( NCashEmailHelper.class );
	public static final String EMAIL_ENCODING = "UTF-8";

	public static void sendEmailWithAttachment( Properties properties, List<String> toAddresses, List<String> ccAddresses, List<String> bccAddresses, String subject, String body, List<File> attachedFiles ) throws NCashEmailException
	{
		if ( !hasAddresses( toAddresses ) && !hasAddresses( ccAddresses ) && !hasAddresses( bccAddresses ) )
			throw new NCashEmailException( "No recipients found." );

		Session session = Session.getInstance( properties, new NcashMailAuthenticator( properties ) );

		final MimeMessage message = new MimeMessage( session );
		try
		{
			message.setSentDate( new Date() );
			message.setSubject( subject );

			addRecipients( message, toAddresses, RecipientType.TO );
			addRecipients( message, ccAddresses, RecipientType.CC );
			addRecipients( message, bccAddresses, RecipientType.BCC );

			MimeBodyPart msgBodyPart = new MimeBodyPart();
			msgBodyPart.setContent( body, "text/html; charset=" + EMAIL_ENCODING );

			Multipart multiPart = new MimeMultipart();
			multiPart.addBodyPart( msgBodyPart );

			if ( attachedFiles != null && attachedFiles.size() != 0 )
			{
				for ( File attachedFile : attachedFiles )
				{
					MimeBodyPart msgAttachement = new MimeBodyPart();
					DataSource source = new FileDataSource( attachedFile.getAbsoluteFile() );
					msgAttachement.setDataHandler( new DataHandler( source ) );
					msgAttachement.setFileName( attachedFile.getName() );
					multiPart.addBodyPart( msgAttachement );
				}
			}

			message.setContent( multiPart );
		}
		catch ( MessagingException e )
		{
			throw new NCashEmailException( "Failed to create E-Mail Message", e );
		}

		try
		{
			Transport.send( message );
		}
		catch ( AuthenticationFailedException e )
		{
			throw new NCashEmailException( "Authentication failed. Check your SMTP configuration.", e );
		}
		catch ( Exception e )
		{
			log.warn( "Failed to send email message", e );
			throw new NCashEmailException( "Failed to send email message", e );
		}
	}

	public static void sendMail( Properties properties, List<String> toAddresses, List<String> ccAddresses, List<String> bccAddresses, String subject, String body, String attach ) throws NCashEmailException
	{
		// sanity check
		if ( !hasAddresses( toAddresses ) && !hasAddresses( ccAddresses ) )
			return;

		// get a default mailer session instance
		Session session = Session.getInstance( properties, new NcashMailAuthenticator( properties ) );

		// create a new message
		final Message messageF = new MimeMessage( session );

		try
		{
			// set the message fields
			messageF.setSubject( subject );
			//messageF.setText( body );
			messageF.setSentDate( new Date() );

			// add the message recipients
			addRecipients( messageF, toAddresses, Message.RecipientType.TO );
			addRecipients( messageF, ccAddresses, Message.RecipientType.CC );
			addRecipients( messageF, bccAddresses, RecipientType.BCC );

			MimeBodyPart messagePart = new MimeBodyPart();
			messagePart.setText( body );

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart( messagePart );

			if ( attach != null )
			{
				MimeBodyPart attachmentPart = new MimeBodyPart();
				FileDataSource fileDataSource = new FileDataSource( attach );

				attachmentPart.setDataHandler( new DataHandler( fileDataSource ) );
				attachmentPart.setFileName( fileDataSource.getFile().getName() );
				multipart.addBodyPart( attachmentPart );
			}

			messageF.setContent( multipart );

		}
		catch ( MessagingException e )
		{
			throw new NCashEmailException( "Failed to build mail message", e );
		}

		// send the message in it's own thread
		new Thread( new Runnable()
		{
			public void run()
			{
				try
				{
					Transport.send( messageF );
				}
				catch ( MessagingException e )
				{
					log.warn( "Failed to send email message", e );
				}
			}
		}, "Send Message" ).start();
	}

	public static void sendHtmlMail( Properties properties, List<String> toAddresses, List<String> ccAddresses, List<String> bccAddresses, String subject, String htmlContent, List<EmailImageParam> imageParams ) throws NCashEmailException
	{
		// sanity check
		if ( !hasAddresses( toAddresses ) && !hasAddresses( ccAddresses ) )
			return;

		// get a default mailer session instance
		Session session = Session.getInstance( properties, new NcashMailAuthenticator( properties ) );

		// create a new message
		final MimeMessage messageF = new MimeMessage( session );

		try
		{
			// set the message fields
			messageF.setSubject( subject );
			//messageF.setText( body );
			messageF.setSentDate( new Date() );

			// add the message recipients
			addRecipients( messageF, toAddresses, Message.RecipientType.TO );
			addRecipients( messageF, ccAddresses, Message.RecipientType.CC );
			addRecipients( messageF, bccAddresses, RecipientType.BCC );

			MimeBodyPart messagePart = new MimeBodyPart();

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart( messagePart );

			final MimeMessageHelper message = new MimeMessageHelper( messageF, true, NCashEmailHelper.EMAIL_ENCODING );
			message.setText( htmlContent, true );
			for ( EmailImageParam imageParam : imageParams )
			{
				// Add the inline image, referenced from the HTML code as "cid:${imageResourceName}"
				final InputStreamSource imageSource = new ByteArrayResource( imageParam.getImageContents() );
				message.addInline( imageParam.getImageContentId(), imageSource, imageParam.getImageType() );
			}

		}
		catch ( MessagingException e )
		{
			throw new NCashEmailException( "Failed to build mail message", e );
		}

		// send the message in it's own thread
		new Thread( new Runnable()
		{
			public void run()
			{
				try
				{
					Transport.send( messageF );
				}
				catch ( MessagingException e )
				{
					log.warn( "Failed to send email message", e );
				}
			}
		}, "Send Message" ).start();
	}

	private static boolean hasAddresses( List<String> addresses )
	{
		// sanity check
		if ( addresses == null )
			return false;

		// check each address to see if we have any specified
		for ( String address : addresses )
		{
			if ( !StringHelper.isEmpty( address ) )
				return true;
		}

		// if we got here we didn't find any addresses
		return false;
	}

	private static void addRecipients( Message message, List<String> addresses, Message.RecipientType type ) throws MessagingException
	{
		// sanity check
		if ( addresses == null )
			return;

		// add each of the address from the list to our message
		for ( String address : addresses )
		{
			// ignore if this address is empty
			if ( StringHelper.isEmpty( address ) )
				continue;

			// add to the message
			InternetAddress intAddress = new InternetAddress( address, false );
			intAddress.validate();
			message.addRecipient( type, intAddress );
		}
	}
}
