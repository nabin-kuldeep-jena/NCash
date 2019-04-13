package com.asjngroup.ncash.common.hibernate.generate;

import com.asjngroup.ncash.common.exception.NCashException;

public class GenerateException extends NCashException {
	private static final long serialVersionUID = 1L;

	public GenerateException(String paramString) {
		super(paramString);
	}

	public GenerateException(Throwable paramThrowable) {
		super(paramThrowable);
	}

	public GenerateException(String paramString, Object[] paramArrayOfObject) {
		super(paramString, paramArrayOfObject);
	}

	public GenerateException(String paramString, Throwable paramThrowable,
			Object[] paramArrayOfObject) {
		super(paramString, paramThrowable, paramArrayOfObject);
	}
}