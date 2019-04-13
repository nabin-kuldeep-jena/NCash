package com.asjngroup.ncash.addons.razorpay.webhook.exception;

import com.asjngroup.ncash.common.exception.NCashException;
import com.asjngroup.ncash.common.util.StringUtil;

public class PaymentUpdateException extends NCashException
{
	private static final long serialVersionUID = 1L;

	public PaymentUpdateException( String str )
    {
        super( StringUtil.create( str ) );
    }

    public PaymentUpdateException( Throwable t )
    {
        super( "", t );
    }

    public PaymentUpdateException( String str, Object... args )
    {
        super( StringUtil.create( str, args ) );
    }

    public PaymentUpdateException( String str, Throwable t, Object... args )
    {
        super( StringUtil.create( str, args ), t );
    }
}
