package RemoteInterface;


/**
* RemoteInterface/ListOfBoolHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ./ServerRemote.idl
* Tuesday, October 24, 2017 1:55:50 AM EDT
*/

public final class ListOfBoolHolder implements org.omg.CORBA.portable.Streamable
{
  public boolean value[] = null;

  public ListOfBoolHolder ()
  {
  }

  public ListOfBoolHolder (boolean[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = RemoteInterface.ListOfBoolHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    RemoteInterface.ListOfBoolHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return RemoteInterface.ListOfBoolHelper.type ();
  }

}
