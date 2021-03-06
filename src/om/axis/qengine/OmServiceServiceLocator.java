/* OpenMark online assessment system
   Copyright (C) 2007 The Open University

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/**
 * OmServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package om.axis.qengine;

@SuppressWarnings(value = { "rawtypes", "serial", "unchecked" })
public class OmServiceServiceLocator extends org.apache.axis.client.Service implements om.axis.qengine.OmServiceService {

    public OmServiceServiceLocator() {
    }


    public OmServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public OmServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for Om
    private java.lang.String Om_address = "http://localhost:8080/om-qe/services/Om";

    public java.lang.String getOmAddress() {
        return Om_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String OmWSDDServiceName = "Om";

    public java.lang.String getOmWSDDServiceName() {
        return OmWSDDServiceName;
    }

    public void setOmWSDDServiceName(java.lang.String name) {
        OmWSDDServiceName = name;
    }

    public om.axis.qengine.OmService getOm() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(Om_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getOm(endpoint);
    }

    public om.axis.qengine.OmService getOm(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            om.axis.qengine.OmSoapBindingStub _stub = new om.axis.qengine.OmSoapBindingStub(portAddress, this);
            _stub.setPortName(getOmWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setOmEndpointAddress(java.lang.String address) {
        Om_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (om.axis.qengine.OmService.class.isAssignableFrom(serviceEndpointInterface)) {
                om.axis.qengine.OmSoapBindingStub _stub = new om.axis.qengine.OmSoapBindingStub(new java.net.URL(Om_address), this);
                _stub.setPortName(getOmWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("Om".equals(inputPortName)) {
            return getOm();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://om.open.ac.uk/", "OmServiceService");
    }

    private java.util.HashSet ports = null;

	public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://om.open.ac.uk/", "Om"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("Om".equals(portName)) {
            setOmEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
