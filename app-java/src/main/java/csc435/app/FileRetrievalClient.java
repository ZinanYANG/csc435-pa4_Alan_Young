package csc435.app;

public class FileRetrievalClient
{
    public static void main(String[] args)
    {
        ClientSideEngine engine = new ClientSideEngine();
        ClientAppInterface appInterface = new ClientAppInterface(engine);
        appInterface.readCommands();
    }
}
