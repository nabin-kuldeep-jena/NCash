package com.asjngroup.ncash.email.util;

public class EmailImageParam
{
	//can pass image name
	private String imageContentId;

	private String imageType;

	private byte[] imageContents;

	public String getImageContentId()
	{
		return imageContentId;
	}

	public void setImageContentId( String imageContentId )
	{
		this.imageContentId = imageContentId;
	}

	public String getImageType()
	{
		return imageType;
	}

	public void setImageType( String imageType )
	{
		this.imageType = imageType;
	}

	public byte[] getImageContents()
	{
		return imageContents;
	}

	public void setImageContents( byte[] imageContents )
	{
		this.imageContents = imageContents;
	}
}
