package uidxgenerator.domain;

import java.io.Serializable;

import uidxgenerator.util.StringUtil;

/**
 * SQL����\��Domain�ł��B
 * @author W.Ryozo
 * @version 1.0
 */
public class SqlCommand implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** SQL�{�� */
	private String command;
	
	/**
	 * SQL����SQL��ʂ𗘗p���ăC���X�^���X���쐬���܂��B
	 * @param command SQL��
	 * @param type SQL�̎��
	 */
	public SqlCommand(String command) {
		if (StringUtil.isNullOrEmpty(command)) {
			throw new IllegalArgumentException("sql is empty");
		}
		this.command = command;
	}
	
	/**
	 * SQL�{�����擾���܂��B
	 * @return
	 */
	public String getSqlCommand() {
		return command;
	}

	@Override
	public String toString() {
		return command;
	}
}