package RemoteInterface;


/**
* RemoteInterface/ServerRemoteHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ./ServerRemote.idl
* Monday, October 30, 2017 2:22:36 PM EDT
*/

abstract public class ServerRemoteHelper
{
  private static String  _id = "IDL:RemoteInterface/ServerRemote:1.0";

  public static void insert (org.omg.CORBA.Any a, RemoteInterface.ServerRemote that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static RemoteInterface.ServerRemote extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (RemoteInterface.ServerRemoteHelper.id (), "ServerRemote");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static RemoteInterface.ServerRemote read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_ServerRemoteStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, RemoteInterface.ServerRemote value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static RemoteInterface.ServerRemote narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof RemoteInterface.ServerRemote)
      return (RemoteInterface.ServerRemote)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      RemoteInterface._ServerRemoteStub stub = new RemoteInterface._ServerRemoteStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static RemoteInterface.ServerRemote unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof RemoteInterface.ServerRemote)
      return (RemoteInterface.ServerRemote)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      RemoteInterface._ServerRemoteStub stub = new RemoteInterface._ServerRemoteStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
