package com.jindi.infra.space.wrapper;

import static com.jindi.infra.space.constant.OperatorConsts.*;

import java.util.*;
import java.util.function.Consumer;

import org.springframework.util.StringUtils;

import com.jindi.infra.space.lambda.ColumnLambda;

public class WhereWrapper {

	private List<CompositeItem> compositeItemList = new ArrayList<>();
	private Map<String, Object> options = new HashMap<>();

	public List<CompositeItem> getCompositeItemList() {
		return compositeItemList;
	}

	public Map<String, Object> getOptions() {
		return options;
	}

	public WhereWrapper eq(String fieldName, Object value) {
		compositeItemList.add(new CompositeItem(fieldName, EQ, value));
		return this;
	}

	public <T> WhereWrapper eq(ColumnLambda.SFunction<T, ?> fn, Object value) {
		String fieldName = ColumnLambda.getFieldName(fn);
		compositeItemList.add(new CompositeItem(fieldName, EQ, value));
		return this;
	}

	public WhereWrapper gt(String fieldName, Object value) {
		compositeItemList.add(new CompositeItem(fieldName, GT, value));
		return this;
	}

	public <T> WhereWrapper gt(ColumnLambda.SFunction<T, ?> fn, Object value) {
		String fieldName = ColumnLambda.getFieldName(fn);
		compositeItemList.add(new CompositeItem(fieldName, GT, value));
		return this;
	}

	public WhereWrapper gte(String fieldName, Object value) {
		compositeItemList.add(new CompositeItem(fieldName, GTE, value));
		return this;
	}

	public <T> WhereWrapper gte(ColumnLambda.SFunction<T, ?> fn, Object value) {
		String fieldName = ColumnLambda.getFieldName(fn);
		compositeItemList.add(new CompositeItem(fieldName, GTE, value));
		return this;
	}

	public WhereWrapper lt(String fieldName, Object value) {
		compositeItemList.add(new CompositeItem(fieldName, LT, value));
		return this;
	}

	public <T> WhereWrapper lt(ColumnLambda.SFunction<T, ?> fn, Object value) {
		String fieldName = ColumnLambda.getFieldName(fn);
		compositeItemList.add(new CompositeItem(fieldName, LT, value));
		return this;
	}

	public WhereWrapper lte(String fieldName, Object value) {
		compositeItemList.add(new CompositeItem(fieldName, LTE, value));
		return this;
	}

	public <T> WhereWrapper lte(ColumnLambda.SFunction<T, ?> fn, Object value) {
		String fieldName = ColumnLambda.getFieldName(fn);
		compositeItemList.add(new CompositeItem(fieldName, LTE, value));
		return this;
	}

	public WhereWrapper in(String fieldName, Object value) {
		compositeItemList.add(new CompositeItem(fieldName, IN, value));
		return this;
	}

	public <T> WhereWrapper in(ColumnLambda.SFunction<T, ?> fn, Object value) {
		String fieldName = ColumnLambda.getFieldName(fn);
		compositeItemList.add(new CompositeItem(fieldName, IN, value));
		return this;
	}

	public WhereWrapper nin(String fieldName, Object value) {
		compositeItemList.add(new CompositeItem(fieldName, NIN, value));
		return this;
	}

	public <T> WhereWrapper nin(ColumnLambda.SFunction<T, ?> fn, Object value) {
		String fieldName = ColumnLambda.getFieldName(fn);
		compositeItemList.add(new CompositeItem(fieldName, NIN, value));
		return this;
	}

	public WhereWrapper like(String fieldName, Object value) {
		compositeItemList.add(new CompositeItem(fieldName, LIKE, String.format(LIKE_TEMPLATE, value)));
		return this;
	}

	public <T> WhereWrapper like(ColumnLambda.SFunction<T, ?> fn, Object value) {
		String fieldName = ColumnLambda.getFieldName(fn);
		compositeItemList.add(new CompositeItem(fieldName, LIKE, String.format(LIKE_TEMPLATE, value)));
		return this;
	}

	public WhereWrapper lLike(String fieldName, Object value) {
		compositeItemList.add(new CompositeItem(fieldName, LIKE, String.format(L_LIKE_TEMPLATE, value)));
		return this;
	}

	public <T> WhereWrapper lLike(ColumnLambda.SFunction<T, ?> fn, Object value) {
		String fieldName = ColumnLambda.getFieldName(fn);
		compositeItemList.add(new CompositeItem(fieldName, LIKE, String.format(L_LIKE_TEMPLATE, value)));
		return this;
	}

	public WhereWrapper rLike(String fieldName, Object value) {
		compositeItemList.add(new CompositeItem(fieldName, LIKE, String.format(R_LIKE_TEMPLATE, value)));
		return this;
	}

	public <T> WhereWrapper rLike(ColumnLambda.SFunction<T, ?> fn, Object value) {
		String fieldName = ColumnLambda.getFieldName(fn);
		compositeItemList.add(new CompositeItem(fieldName, LIKE, String.format(R_LIKE_TEMPLATE, value)));
		return this;
	}

	public WhereWrapper ne(String fieldName, Object value) {
		compositeItemList.add(new CompositeItem(fieldName, NE, value));
		return this;
	}

	public <T> WhereWrapper ne(ColumnLambda.SFunction<T, ?> fn, Object value) {
		String fieldName = ColumnLambda.getFieldName(fn);
		compositeItemList.add(new CompositeItem(fieldName, NE, value));
		return this;
	}

	public WhereWrapper isNull(String fieldName, Boolean value) {
		compositeItemList.add(new CompositeItem(fieldName, IS_NULL, value));
		return this;
	}

	public <T> WhereWrapper isNull(ColumnLambda.SFunction<T, ?> fn, Boolean value) {
		String fieldName = ColumnLambda.getFieldName(fn);
		compositeItemList.add(new CompositeItem(fieldName, IS_NULL, value));
		return this;
	}

	public <T> WhereWrapper or(Consumer<WhereWrapper> consumer) {
		WhereWrapper wrapper = new WhereWrapper();
		consumer.accept(wrapper);
		compositeItemList.add(new CompositeItem(null, OR, wrapper.getCompositeItemList()));
		return this;
	}

	public <T> WhereWrapper and(Consumer<WhereWrapper> consumer) {
		WhereWrapper wrapper = new WhereWrapper();
		consumer.accept(wrapper);
		compositeItemList.add(new CompositeItem(null, AND, wrapper.getCompositeItemList()));
		return this;
	}

	public WhereWrapper option(String option, Object value) {
		if (!StringUtils.isEmpty(option)) {
			options.put(option, value);
		}
		return this;
	}
}
