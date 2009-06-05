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
 * Results.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package om.axis.qengine;

public class Results  implements java.io.Serializable {
    private java.lang.String actionSummary;
    private java.lang.String answerLine;
    private int attempts;
    private om.axis.qengine.CustomResult[] customResults;
    private java.lang.String questionLine;
    private int[] score;
    private om.axis.qengine.Score[] scores;

    public Results() {
    }

    public Results(
           java.lang.String actionSummary,
           java.lang.String answerLine,
           int attempts,
           om.axis.qengine.CustomResult[] customResults,
           java.lang.String questionLine,
           int[] score,
           om.axis.qengine.Score[] scores) {
           this.actionSummary = actionSummary;
           this.answerLine = answerLine;
           this.attempts = attempts;
           this.customResults = customResults;
           this.questionLine = questionLine;
           this.score = score;
           this.scores = scores;
    }


    /**
     * Gets the actionSummary value for this Results.
     * 
     * @return actionSummary
     */
    public java.lang.String getActionSummary() {
        return actionSummary;
    }


    /**
     * Sets the actionSummary value for this Results.
     * 
     * @param actionSummary
     */
    public void setActionSummary(java.lang.String actionSummary) {
        this.actionSummary = actionSummary;
    }


    /**
     * Gets the answerLine value for this Results.
     * 
     * @return answerLine
     */
    public java.lang.String getAnswerLine() {
        return answerLine;
    }


    /**
     * Sets the answerLine value for this Results.
     * 
     * @param answerLine
     */
    public void setAnswerLine(java.lang.String answerLine) {
        this.answerLine = answerLine;
    }


    /**
     * Gets the attempts value for this Results.
     * 
     * @return attempts
     */
    public int getAttempts() {
        return attempts;
    }


    /**
     * Sets the attempts value for this Results.
     * 
     * @param attempts
     */
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }


    /**
     * Gets the customResults value for this Results.
     * 
     * @return customResults
     */
    public om.axis.qengine.CustomResult[] getCustomResults() {
        return customResults;
    }


    /**
     * Sets the customResults value for this Results.
     * 
     * @param customResults
     */
    public void setCustomResults(om.axis.qengine.CustomResult[] customResults) {
        this.customResults = customResults;
    }


    /**
     * Gets the questionLine value for this Results.
     * 
     * @return questionLine
     */
    public java.lang.String getQuestionLine() {
        return questionLine;
    }


    /**
     * Sets the questionLine value for this Results.
     * 
     * @param questionLine
     */
    public void setQuestionLine(java.lang.String questionLine) {
        this.questionLine = questionLine;
    }


    /**
     * Gets the score value for this Results.
     * 
     * @return score
     */
    public int[] getScore() {
        return score;
    }


    /**
     * Sets the score value for this Results.
     * 
     * @param score
     */
    public void setScore(int[] score) {
        this.score = score;
    }

    public int getScore(int i) {
        return this.score[i];
    }

    public void setScore(int i, int _value) {
        this.score[i] = _value;
    }


    /**
     * Gets the scores value for this Results.
     * 
     * @return scores
     */
    public om.axis.qengine.Score[] getScores() {
        return scores;
    }


    /**
     * Sets the scores value for this Results.
     * 
     * @param scores
     */
    public void setScores(om.axis.qengine.Score[] scores) {
        this.scores = scores;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Results)) return false;
        Results other = (Results) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.actionSummary==null && other.getActionSummary()==null) || 
             (this.actionSummary!=null &&
              this.actionSummary.equals(other.getActionSummary()))) &&
            ((this.answerLine==null && other.getAnswerLine()==null) || 
             (this.answerLine!=null &&
              this.answerLine.equals(other.getAnswerLine()))) &&
            this.attempts == other.getAttempts() &&
            ((this.customResults==null && other.getCustomResults()==null) || 
             (this.customResults!=null &&
              java.util.Arrays.equals(this.customResults, other.getCustomResults()))) &&
            ((this.questionLine==null && other.getQuestionLine()==null) || 
             (this.questionLine!=null &&
              this.questionLine.equals(other.getQuestionLine()))) &&
            ((this.score==null && other.getScore()==null) || 
             (this.score!=null &&
              java.util.Arrays.equals(this.score, other.getScore()))) &&
            ((this.scores==null && other.getScores()==null) || 
             (this.scores!=null &&
              java.util.Arrays.equals(this.scores, other.getScores())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getActionSummary() != null) {
            _hashCode += getActionSummary().hashCode();
        }
        if (getAnswerLine() != null) {
            _hashCode += getAnswerLine().hashCode();
        }
        _hashCode += getAttempts();
        if (getCustomResults() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCustomResults());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCustomResults(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getQuestionLine() != null) {
            _hashCode += getQuestionLine().hashCode();
        }
        if (getScore() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getScore());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getScore(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getScores() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getScores());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getScores(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Results.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://om.open.ac.uk/", "Results"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("actionSummary");
        elemField.setXmlName(new javax.xml.namespace.QName("", "actionSummary"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("answerLine");
        elemField.setXmlName(new javax.xml.namespace.QName("", "answerLine"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("attempts");
        elemField.setXmlName(new javax.xml.namespace.QName("", "attempts"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("customResults");
        elemField.setXmlName(new javax.xml.namespace.QName("", "customResults"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://om.open.ac.uk/", "CustomResult"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("questionLine");
        elemField.setXmlName(new javax.xml.namespace.QName("", "questionLine"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("score");
        elemField.setXmlName(new javax.xml.namespace.QName("", "score"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("scores");
        elemField.setXmlName(new javax.xml.namespace.QName("", "scores"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://om.open.ac.uk/", "Score"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
