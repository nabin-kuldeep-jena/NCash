package com.asjngroup.ncash.installer.tasks;

public interface InstallerTask
{
    public void initialise() throws InstallerTaskException;
    
    public void setData(InstallerData istallerData);

    public void execute() throws InstallerTaskException;

    public boolean isSilentInstallation();

	public void setSilentInstallation(boolean isSilent);
	
	public void postInstallationTaskInitialise() throws InstallerTaskException;
}
