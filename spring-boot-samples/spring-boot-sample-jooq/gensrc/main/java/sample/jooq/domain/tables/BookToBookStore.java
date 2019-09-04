/**
 * This class is generated by jOOQ
 */
package sample.jooq.domain.tables;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;
import sample.jooq.domain.Keys;
import sample.jooq.domain.Public;
import sample.jooq.domain.tables.records.BookToBookStoreRecord;

/**
 * This class is generated by jOOQ.
 */
@Generated(value = { "https://www.jooq.org",
		"jOOQ version:3.6.2" }, comments = "This class is generated by jOOQ")
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class BookToBookStore extends TableImpl<BookToBookStoreRecord> {

	private static final long serialVersionUID = -557222072;

	/**
	 * The reference instance of <code>PUBLIC.BOOK_TO_BOOK_STORE</code>
	 */
	public static final BookToBookStore BOOK_TO_BOOK_STORE = new BookToBookStore();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<BookToBookStoreRecord> getRecordType() {
		return BookToBookStoreRecord.class;
	}

	/**
	 * The column <code>PUBLIC.BOOK_TO_BOOK_STORE.NAME</code>.
	 */
	public final TableField<BookToBookStoreRecord, String> NAME = createField("NAME",
			org.jooq.impl.SQLDataType.VARCHAR.length(400).nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.BOOK_TO_BOOK_STORE.BOOK_ID</code>.
	 */
	public final TableField<BookToBookStoreRecord, Integer> BOOK_ID = createField(
			"BOOK_ID", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.BOOK_TO_BOOK_STORE.STOCK</code>.
	 */
	public final TableField<BookToBookStoreRecord, Integer> STOCK = createField("STOCK",
			org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * Create a <code>PUBLIC.BOOK_TO_BOOK_STORE</code> table reference
	 */
	public BookToBookStore() {
		this("BOOK_TO_BOOK_STORE", null);
	}

	/**
	 * Create an aliased <code>PUBLIC.BOOK_TO_BOOK_STORE</code> table reference
	 */
	public BookToBookStore(String alias) {
		this(alias, BOOK_TO_BOOK_STORE);
	}

	private BookToBookStore(String alias, Table<BookToBookStoreRecord> aliased) {
		this(alias, aliased, null);
	}

	private BookToBookStore(String alias, Table<BookToBookStoreRecord> aliased,
			Field<?>[] parameters) {
		super(alias, Public.PUBLIC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<BookToBookStoreRecord> getPrimaryKey() {
		return Keys.CONSTRAINT_2;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<BookToBookStoreRecord>> getKeys() {
		return Arrays.<UniqueKey<BookToBookStoreRecord>>asList(Keys.CONSTRAINT_2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ForeignKey<BookToBookStoreRecord, ?>> getReferences() {
		return Arrays.<ForeignKey<BookToBookStoreRecord, ?>>asList(
				Keys.FK_B2BS_BOOK_STORE, Keys.FK_B2BS_BOOK);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BookToBookStore as(String alias) {
		return new BookToBookStore(alias, this);
	}

	/**
	 * Rename this table
	 */
	public BookToBookStore rename(String name) {
		return new BookToBookStore(name, null);
	}
}
