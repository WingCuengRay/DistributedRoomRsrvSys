package RemoteInterface;


/**
* RemoteInterface/ListOfStringHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ./ServerRemote.idl
* Tuesday, October 24, 2017 1:55:50 AM EDT
*/

public final class ListOfStringHolder implements org.omg.CORBA.portable.Streamable
{
  public String value[] = null;

  public ListOfStringHolder ()
  {
  }

  public ListOfStringHolder (String[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = RemoteInterface.ListOfStringHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    RemoteInterface.ListOfStringHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return RemoteInterface.ListOfStringHelper.type ();
  }

}
