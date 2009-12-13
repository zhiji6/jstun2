/*
 * This file is part of JSTUN.
 * 
 * Copyright (c) 2005 Thomas King <king@t-king.de> - All rights
 * reserved.
 * 
 * This software is licensed under either the GNU Public License (GPL),
 * or the Apache 2.0 license. Copies of both license agreements are
 * included in this distribution.
 */

package de.javawi.jstun.attribute;

import java.util.logging.Logger;

import de.javawi.jstun.attribute.exception.MessageAttributeParsingException;
import de.javawi.jstun.attribute.exception.UnknownMessageAttributeException;
import de.javawi.jstun.attribute.legacy.Password;
import de.javawi.jstun.util.Utility;
import de.javawi.jstun.util.UtilityException;

public abstract class AbstractMessageAttribute implements MessageAttributeInterface {
	private static Logger logger = Logger.getLogger("de.javawi.stun.util.MessageAttribute");

	/*
	    0                   1                   2                   3
	    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |         Type                  |            Length             |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |                         Value (variable)                ....
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	*/

	MessageAttributeType type;

	public AbstractMessageAttribute() {
	}

	public AbstractMessageAttribute(MessageAttributeType type) {
		setType(type);
	}

	/**
	 * Sets the {@link MessageAttributeType}
	 * 
	 * @param type
	 */
	private void setType(MessageAttributeType type) {
		this.type = type;
	}

	public MessageAttribute.MessageAttributeType getType() {
		return type;
	}

	public static int typeToInteger(MessageAttributeType type) {
		return type.getEncoding();
	}

	public static MessageAttributeType longToType(long type) { // TODO why long?
		MessageAttributeType[] values = MessageAttributeInterface.MessageAttributeType
				.values();

		for (MessageAttributeInterface.MessageAttributeType ma : values) {
			if (type == ma.getEncoding())
				return ma;
		}
		return null; // TODO should throw exception??
	}

	// TODO decide which one to keep
	public static MessageAttributeType intToType(int type) {
		MessageAttributeType[] values = MessageAttributeInterface.MessageAttributeType
				.values();

		for (MessageAttributeInterface.MessageAttributeType mat : values) {
			if (type == mat.getEncoding())
				return mat;
		}
		return null; // TODO should throw exception??
	}

	abstract public byte[] getBytes() throws UtilityException;
	// abstract public MessageAttribute parse(byte[] data) throws
	// MessageAttributeParsingException;

	public int getLength() throws UtilityException {
		int length = getBytes().length;
		return length;
	}

	public static AbstractMessageAttribute parseCommonHeader(byte[] data)
			throws MessageAttributeParsingException {
		try {

			byte[] typeArray = new byte[2];
			System.arraycopy(data, 0, typeArray, 0, 2);
			int type = Utility.twoBytesToInteger(typeArray);

			byte[] lengthArray = new byte[2];
			System.arraycopy(data, 2, lengthArray, 0, 2);
			int lengthValue = Utility.twoBytesToInteger(lengthArray);

			byte[] valueArray = new byte[lengthValue];
			System.arraycopy(data, 4, valueArray, 0, lengthValue);

			AbstractMessageAttribute ma;
			// MessageAttributeType mat = intToType(type);

			MessageAttributeType[] values = MessageAttributeInterface.MessageAttributeType
					.values();

			for (MessageAttributeInterface.MessageAttributeType mat : values) {
				if (type == mat.getEncoding()) {
					String klassName = mat.toString();
					Class klass = Class.forName(klassName);

				}
				// ma =
			}

			switch (type) {
				case MAPPEDADDRESS :
					ma = MappedAddress.parse(valueArray);
					break;
				case USERNAME :
					ma = Username.parse(valueArray);
					break;
				case PASSWORD :
					ma = Password.parse(valueArray);
					break;
				case MESSAGEINTEGRITY :
					ma = MessageIntegrity.parse(valueArray);
					break;
				case ERRORCODE :
					ma = ErrorCode.parse(valueArray);
					break;
				case UNKNOWNATTRIBUTE :
					ma = UnknownAttribute.parse(valueArray);
					break;
				default :
					if (type <= 0x7fff) {
						throw new UnknownMessageAttributeException("Unkown mandatory message attribute", longToType(type));
					} else {
						logger.finer("MessageAttribute with type " + type + " unkown.");
						ma = Dummy.parse(valueArray);
						break;
					}
			}
			return ma;
		} catch (UtilityException ue) {
			throw new MessageAttributeParsingException("Parsing error");
		}
	}
}