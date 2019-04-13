package com.asjngroup.ncash.email.util.sender;

import java.util.List;

import com.asjngroup.ncash.email.util.EmailTemplateParam;

public interface EmailSender
{
	public void sendPlainEmailMessage( List<String> toAddresses, List<String> ccAddresses, List<String> bccAddresses, String subject, String emailMessage ) throws EmailSendingFailedEception;

	public void sendHtmlTemplateEmailMessage( EmailTemplateParam emailTemplateParam ) throws EmailSendingFailedEception;
}
