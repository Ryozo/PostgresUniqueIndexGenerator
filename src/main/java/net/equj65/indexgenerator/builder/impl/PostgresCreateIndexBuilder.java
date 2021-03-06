package net.equj65.indexgenerator.builder.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.equj65.indexgenerator.builder.ICreateIndexBuilder;
import net.equj65.indexgenerator.constants.PostgresConditionLiteral;
import net.equj65.indexgenerator.constants.PostgresDataType;
import net.equj65.indexgenerator.domain.SqlCommand;
import net.equj65.indexgenerator.util.StringUtils;
import static net.equj65.indexgenerator.constants.SqlConstants.*;

/**
 * PostgreSQL用のCreateUniqueIndex文Builderです。
 * 
 * @author W.Ryozo
 * @version 1.0
 */
public class PostgresCreateIndexBuilder implements ICreateIndexBuilder {
	
	/** SQL文のベース */
	private static final String SQL_BASE = "CREATE UNIQUE INDEX {IDX_NAME} ON {TABLE_NAME} ({KEY_LIST})";
	private static final String SQL_WHERE = " WHERE ";
	private static final String SQL_CONDITION = "{FIELD_NAME} = {FIELD_VALUE}";
	private static final String SQL_AND = "AND";
	/** デフォルトのIndex名定義 */
	private static final String DEFAULT_INDEX_NAME = "{TABLE_NAME}_{KEY_LIST}_key";
	/** 置換文字列 */
	private static final String REPLACE_STR_INDEX_NAME = "{IDX_NAME}";
	private static final String REPLACE_STR_TABLE_NAME = "{TABLE_NAME}";
	private static final String REPLACE_STR_KEY_LIST = "{KEY_LIST}";
	private static final String REPLACE_STR_FIELD_NAME = "{FIELD_NAME}";
	private static final String REPLACE_STR_FIELD_VALUE = "{FIELD_VALUE}";
	
	/** Index名称 */
	// TODO index名は指定もできるし、指定しなくてもいい（デフォルト値の設定）する。
	private String indexName;
	/** テーブル名称 */
	private String tableName;
	/** 一意キー項目 */
	private String[] keyList;
	/** 一意条件 */
	private Map<String, Object> conditionMap = new LinkedHashMap<>();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTableName(String tableName) {
		if (StringUtils.isNullOrEmpty(tableName)) {
			throw new IllegalArgumentException("対象テーブル名が指定されていません。");
		}
		this.tableName = tableName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIndexFields(String... fields) {
		if (fields == null || fields.length == 0) {
			throw new IllegalArgumentException("インデックス付与対象のカラムが指定されていません。");
		}
		this.keyList = fields;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addIndexCondition(String fieldName, Object fieldValue) {
		if (StringUtils.isNullOrEmpty(fieldName)) {
			throw new IllegalArgumentException("UniqueIndexの条件フィールドがNullです");
		}
		
		if (!PostgresDataType.isSupport(fieldValue)) {
			throw new IllegalArgumentException("条件値がサポート外のデータ型です。 : " + fieldValue.getClass());
		}

		conditionMap.put(fieldName, fieldValue);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIndexName(String name) {
		this.indexName = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SqlCommand build() {
		if (tableName == null || keyList == null) {
			// TODO Exceptionを定義する。
			throw new RuntimeException();
		}
		if (StringUtils.isNullOrEmpty(indexName)) {
			indexName = DEFAULT_INDEX_NAME
					.replace(REPLACE_STR_TABLE_NAME, tableName)
					.replace(REPLACE_STR_KEY_LIST, StringUtils.join(keyList, "_"));
		}

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(
				SQL_BASE.replace(REPLACE_STR_INDEX_NAME, indexName)
						.replace(REPLACE_STR_TABLE_NAME, tableName)
						.replace(REPLACE_STR_KEY_LIST, StringUtils.join(keyList, ", ")));
		
		if (!conditionMap.isEmpty()) {
			sqlBuilder.append(SQL_WHERE);
			StringBuilder conditionBuilder = new StringBuilder();
			Set<Entry<String, Object>> entrySet = conditionMap.entrySet();
			Iterator<Entry<String, Object>> iterator = entrySet.iterator();
			
			boolean isFirst = true;
			while (iterator.hasNext()) {
				Entry<String, Object> conditionEntry = iterator.next();
				if (!isFirst) {
					conditionBuilder.append(SQL_AND);
					isFirst = false;
				}
				PostgresConditionLiteral literal = PostgresDataType.getLiteral(conditionEntry.getValue());
				conditionBuilder.append(
						SQL_CONDITION.replace(REPLACE_STR_FIELD_NAME, conditionEntry.getKey())
								     .replace(REPLACE_STR_FIELD_VALUE, literal.toLiteralNotation(conditionEntry.getValue())));
			}
			sqlBuilder.append(conditionBuilder.toString());
		}
		
		sqlBuilder.append(SQL_DELIMITER);
		// ツール[ER-MASTER]はどの環境で出力したとしても改行コードをCRLFで出力する。
		// そのため、出力改行コードはCRLF固定とする。
		sqlBuilder.append(LINE_SEPARATOR);
		
		SqlCommand command = new SqlCommand(sqlBuilder.toString());
		return command;
	}
}
