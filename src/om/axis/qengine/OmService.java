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
 * OmService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package om.axis.qengine;

public interface OmService extends java.rmi.Remote {
    public om.axis.qengine.StartReturn start(java.lang.String questionID, java.lang.String questionVersion, java.lang.String questionBaseURL, java.lang.String[] initialParamNames, java.lang.String[] initialParamValues, java.lang.String[] cachedResources) throws java.rmi.RemoteException, om.axis.qengine.OmException;
    public void stop(java.lang.String questionSession) throws java.rmi.RemoteException, om.axis.qengine.OmException;
    public om.axis.qengine.ProcessReturn process(java.lang.String questionSession, java.lang.String[] names, java.lang.String[] values) throws java.rmi.RemoteException, om.axis.qengine.OmException;
    public java.lang.String getEngineInfo() throws java.rmi.RemoteException;
    public java.lang.String getQuestionMetadata(java.lang.String questionID, java.lang.String questionVersion, java.lang.String questionBaseURL) throws java.rmi.RemoteException, om.axis.qengine.OmException;
}
