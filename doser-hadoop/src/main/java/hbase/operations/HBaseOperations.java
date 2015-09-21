package hbase.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellScanner;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.FuzzyRowFilter;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.log4j.Logger;

/**
 * Hbase Operations for version 1.0
 * 
 * @author quh
 * 
 */
public final class HBaseOperations {

	private static final HBaseOperations instance;

	public static final List<String> tableNames = Arrays.asList("DisLTR_Cache",
			"DisLTR_UserResults", "LDADC_EntToSf", "LDADC_SFToEnt",
			"LDADC_Context", "DBPEDIA_CatToEnts", "DBPEDIA_EntityFacts");

	private Date date;

	private Connection conn;

	static {
		try {
			instance = new HBaseOperations();
		} catch (Exception e) {
			throw new RuntimeException(
					"An error occurred during initalization", e);
		}
	}

	public static HBaseOperations getInstance() {
		return instance;
	}

	/**
	 * Constructor Creates a Configuration
	 */
	private HBaseOperations() {
		super();
		this.date = new Date();
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "zaire");
		try {
			conn = ConnectionFactory.createConnection(conf);
		} catch (IOException e) {
			throw new RuntimeException(
					"An error occurred during initalization", e);
		}
		initializeTablesPerDefault();
	}

	/**
	 * Add a new Record in the Hbase table
	 * 
	 * @param tableName
	 *            Name of the table
	 * @param rowKey
	 *            Rowkey
	 * @param family
	 *            HBase family
	 * @param qualifier
	 *            HBase qualifier
	 * @param value
	 *            The value to store
	 * @throws IOException
	 *             Nothing happens!
	 */
	public void addRecord(final String tableName, final String rowKey,
			final String family, final String qualifier, final String value)
			throws IOException {
		final Put put = new Put(Bytes.toBytes(rowKey));
		Cell cell = CellUtil.createCell(Bytes.toBytes(rowKey),
				Bytes.toBytes(family), Bytes.toBytes(qualifier),
				date.getTime(), KeyValue.Type.Put.getCode(),
				Bytes.toBytes(value));
		put.add(cell);
		Table table = null;
		try {
			table = conn.getTable(TableName.valueOf(tableName));
			table.put(put);
		} catch (IOException e) {
			e.printStackTrace();
			Logger.getRootLogger().error(
					"Error while fetching HBase table " + tableName
							+ ". Entry not added!", e);
		} finally {
			if (table != null) {
				try {
					table.close();
				} catch (IOException e) {
					e.printStackTrace();
					Logger.getRootLogger().error(
							"Error IOException while closing HBase table", e);
				}
			}
		}
	}

	/**
	 * Fast Version of addRecord. If a huge number of addrecords must be
	 * performed we fetch the table once by calling getTable. All records are
	 * stored without closing the table object. The user must close table object
	 * manually with closeTable();
	 * 
	 * 
	 * @param table
	 *            The already opened table object
	 * @param rowKey
	 *            Rowkey
	 * @param family
	 *            Hbase family
	 * @param qualifier
	 *            HBase Qualifier
	 * @param value
	 *            The value to store
	 * @throws IOException
	 *             Nothing happens!
	 */
	public void addRecordFast(Table table, final String rowKey,
			final String family, final String qualifier, final String value)
			throws IOException {
		final Put put = new Put(Bytes.toBytes(rowKey));
		put.add(CellUtil.createCell(Bytes.toBytes(rowKey),
				Bytes.toBytes(family), Bytes.toBytes(qualifier),
				date.getTime(), KeyValue.Type.Put.getCode(),
				Bytes.toBytes(value)));
		table.put(put);
	}

	/**
	 * Fetches a table by name.
	 * 
	 * @param tableName
	 *            The tableName to fetch.
	 * @return Table. Return null of table is not available
	 */
	public Table getTable(String tableName) {
		Table table = null;
		try {
			table = conn.getTable(TableName.valueOf(tableName));
		} catch (IOException e) {
			e.printStackTrace();
			Logger.getRootLogger().error(
					"Error while fetching HBase table " + tableName
							+ ". Entry not added!", e);
		}
		return table;
	}

	/**
	 * Closes a table instance
	 * 
	 * @param table
	 *            Table to close
	 */
	public void closeTable(Table table) {
		if (table != null) {
			try {
				table.close();
			} catch (IOException e) {
				e.printStackTrace();
				Logger.getRootLogger().error(
						"Error IOException while closing HBase table", e);
			}
		}
	}

	/**
	 * Adds a complete put object to the table
	 * 
	 * 
	 * @param tableName
	 *            The name of the table
	 * @param put
	 *            An already preconfigured Put object
	 * @throws IOException
	 *             Nothing will be done
	 */
	public void addCompleteEntry(final String tableName, final Put put) {
		Table table = null;
		try {
			table = conn.getTable(TableName.valueOf(tableName));
			table.put(put);
		} catch (IOException e) {
			Logger.getRootLogger().error(
					"Error while fetching HBase table " + tableName
							+ ". Entry not added!", e);
		} finally {
			if (table != null) {
				try {
					table.close();
				} catch (IOException e) {
					Logger.getRootLogger().error(
							"Error IOException while closing HBase table", e);
				}
			}
		}
	}

	/**
	 * Checks the amount if available versions of a specific rowkey. For this
	 * purpose we count the number of different row spellings with the row
	 * prefix wildcardRow
	 * 
	 * @param tableName
	 *            The name of the table to search
	 * @param wildcardRow
	 *            The row prefix which must match with the rownames
	 * @param hashLength
	 *            Length of a default rowkey without version
	 * @return The amount of available versions of row wildcardRow
	 * 
	 *         Return 0 after an IOException
	 * 
	 * @throws IOException
	 */
	public int getAmountRowVersions(final String tableName,
			final String wildcardRow, final int hashLength) {
		int result = 0;

		final String wildcRow = transformRowKey(wildcardRow, hashLength);

		final Scan scan = new Scan();
		scan.setFilter(new FuzzyRowFilter(Arrays
				.asList(new Pair<byte[], byte[]>(Bytes.toBytesBinary(wildcRow
						+ "_\\x00\\x00"), new byte[] { 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 1, 1 }))));
		Table table = null;
		try {
			table = conn.getTable(TableName.valueOf(tableName));
			final ResultScanner res = table.getScanner(scan);
			for (Result r = res.next(); r != null; r = res.next()) {
				result++;
			}
		} catch (IOException e) {
			Logger.getRootLogger().error(
					"Error while fetching HBase table " + tableName
							+ ". Entry not added!", e);
		} finally {
			if (table != null) {
				try {
					table.close();
				} catch (IOException e) {
					Logger.getRootLogger().error(
							"Error IOException while closing HBase table", e);
				}
			}
		}
		return result;
	}

	/**
	 * Remove an Hbase table entry
	 * 
	 * @param tableName
	 *            Name of the Hbase table
	 * @param wildCardRowKey
	 *            The rowkey without standard length and without version
	 * @param family
	 *            HBase family
	 * @param qualifier
	 *            HBase qualifier
	 * @param ver
	 *            The highest version of the entry to delete
	 * @param length
	 *            Length of a default rowkey without version
	 * @return The value of the deleted entry.
	 * 
	 *         Returns null if an IOException occurs.
	 * 
	 * @throws IOException
	 */
	public String removeHbaseEntry(final String tableName,
			final String wildCardRowKey, final String family,
			final String qualifier, final String ver, final int length)
			throws IOException {
		String result = null;
		final String wildcRowKey = transformRowKey(wildCardRowKey, length);
		Table table = null;
		try {
			table = conn.getTable(TableName.valueOf(tableName));
		} catch (IOException e) {
			Logger.getRootLogger().error(
					"Error while fetching HBase table " + tableName
							+ ". Entry not added!", e);
		}
		if (table != null) {
			int version = Integer.valueOf(ver);
			while (version > 0) {
				final String currentRowKey = wildcRowKey + "_"
						+ transformRowKey(String.valueOf(version), 2);
				final Get get = new Get(currentRowKey.getBytes());
				get.addFamily("data".getBytes());
				final Scan scan = new Scan(get);
				final ResultScanner resultScanner = table.getScanner(scan);
				// Result size should be 1
				final Result res = resultScanner.next();
				if (res.containsColumn(family.getBytes(), qualifier.getBytes())) {
					result = Bytes.toString(res.getValue(family.getBytes(),
							qualifier.getBytes()));
					final List<Delete> list = new ArrayList<Delete>();
					final Delete del = new Delete(res.getRow());
					del.addColumn(family.getBytes(), qualifier.getBytes());
					list.add(del);
					table.delete(del);
					break;
				} else {
					version--;
				}

			}

			try {
				table.close();
			} catch (IOException e) {
				Logger.getRootLogger().error(
						"Error IOException while closing HBase table", e);
			}
		}

		return result;
	}

	/**
	 * Method adds 0 values ahead of the string to provide a standard length
	 * 
	 * @param rowKey
	 *            The input string to enrich
	 * @param length
	 *            The length
	 * @return The new String value
	 */
	public String transformRowKey(final String rowKey, final int length) {
		final Integer val = Integer.valueOf(rowKey);
		if (val < 0) {
			String rowK = rowKey.replace("-", "");
			while (rowK.length() < (length - 1)) {
				final StringBuffer buffer = new StringBuffer("0");
				buffer.append(rowKey);
				rowK = buffer.toString();
			}
			final StringBuffer buffer = new StringBuffer("-");
			buffer.append(rowK);
			rowK = buffer.toString();
		} else {
			String rowK = rowKey;
			while (rowK.length() < length) {
				final StringBuffer buffer = new StringBuffer("0");
				buffer.append(rowKey);
				rowK = buffer.toString();
			}
		}
		return rowKey;
	}

	/**
	 * Get all values of a specific row of a specific table
	 * 
	 * If an IOException occurs, the result set is empty or at least incomplete
	 */
	public void getRow(String tableName, String rowKey, String family,
			Set<String> res, int limit) throws IOException {
		Table table = null;
		try {
			table = conn.getTable(TableName.valueOf(tableName));
		} catch (IOException e) {
			Logger.getRootLogger().error(
					"Error while fetching HBase table " + tableName
							+ ". Entry not added!", e);
		}

		if (table == null) {
			return;
		}

		Get get = new Get(rowKey.getBytes());

		if (limit != 0) {
			get.setMaxResultsPerColumnFamily(limit);
		}
		get.addFamily(Bytes.toBytes(family));

		Result rs = table.get(get);
		CellScanner scanner = rs.cellScanner();
		while (scanner.advance()) {
			Cell cell = scanner.current();
			String s = new String(CellUtil.cloneValue(cell));
			if (!s.startsWith(".") && !s.startsWith(",") && s.length() > 2) {
				res.add(s);
			}
		}

		try {
			table.close();
		} catch (IOException e) {
			Logger.getRootLogger().error(
					"Error IOException while closing HBase table", e);
		}
	}

	public Result[] getMultipleRow(final String tableName, final List<Get> gets) {
		Table table = null;
		Result[] res = null;
		try {
			table = conn.getTable(TableName.valueOf(tableName));
		} catch (IOException e) {
			Logger.getRootLogger().error(
					"Error while fetching HBase table " + tableName
							+ ". Entry not added!", e);
		}

		if (table != null) {
			try {
				long time = System.currentTimeMillis();
				res = table.get(gets);
				System.out.println("Fetchtime: "+(System.currentTimeMillis() - time));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	/**
	 * Deletes all entries of row with id rowkey
	 * 
	 * 
	 * @param tableName
	 *            the respective tablename
	 * @param rowKey
	 *            the respective rowkey
	 * @throws IOException
	 *             Nothing will be deleted
	 */
	public void deleteIDRow(String tableName, String rowKey) throws IOException {
		Table table = null;
		try {
			table = conn.getTable(TableName.valueOf(tableName));
			Delete del = new Delete(rowKey.getBytes());
			table.delete(del);
		} catch (IOException e) {
			Logger.getRootLogger().error(
					"Error while fetching HBase table " + tableName
							+ ". Entry not added!", e);
		} finally {
			if (table != null) {
				try {
					table.close();
				} catch (IOException e) {
					Logger.getRootLogger().error(
							"Error IOException while closing HBase table", e);
				}
			}
		}
	}

	private void initializeTablesPerDefault() {
		for (String tableName : tableNames) {
			Admin admin = null;
			try {
				admin = conn.getAdmin();
				if (!admin.tableExists(TableName.valueOf(tableName))) {
					admin.createTable(createTableObject(tableName));
				}
			} catch (IOException e) {
				e.printStackTrace();
				Logger.getRootLogger().error(
						"Error IOException while initializing HBase tables", e);
			} finally {
				if (admin != null) {
					try {
						admin.close();
					} catch (IOException e) {
						Logger.getRootLogger().error(
								"Error IOException while closing HBase admin",
								e);
					}
				}
			}
		}
	}

	private HTableDescriptor createTableObject(final String tableName) {
		final HTableDescriptor tabledesc = new HTableDescriptor(
				TableName.valueOf(tableName));
		HColumnDescriptor descriptor = new HColumnDescriptor("data");
		descriptor.setBloomFilterType(BloomType.ROWCOL);
		tabledesc.addFamily(descriptor);
		return tabledesc;
	}
}