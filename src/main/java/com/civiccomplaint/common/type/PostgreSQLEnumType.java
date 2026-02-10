package com.civiccomplaint.common.type;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import java.util.Properties;

/**
 * Reusable UserType to map Java Enums to PostgreSQL ENUM types.
 * Solves the issue where Hibernate binds Enums as VARCHAR by default, causing
 * PostgreSQL error: "column is of type user_role but expression is of type
 * character varying".
 */
public class PostgreSQLEnumType implements UserType<Enum>, DynamicParameterizedType {

    private Class enumClass;

    @Override
    public void setParameterValues(Properties parameters) {
        final ParameterType reader = (ParameterType) parameters.get(PARAMETER_TYPE);
        if (reader != null) {
            this.enumClass = reader.getReturnedClass();
        }
    }

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<Enum> returnedClass() {
        return enumClass;
    }

    @Override
    public boolean equals(Enum x, Enum y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(Enum x) {
        return Objects.hashCode(x);
    }

    @Override
    public Enum nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        String name = rs.getString(position);
        if (rs.wasNull()) {
            return null;
        }
        // Use the captured enum class to valueOf
        @SuppressWarnings("unchecked")
        Enum result = Enum.valueOf(enumClass, name);
        return result;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Enum value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value.name(), Types.OTHER);
        }
    }

    @Override
    public Enum deepCopy(Enum value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Enum value) {
        return (Serializable) value;
    }

    @Override
    public Enum assemble(Serializable cached, Object owner) {
        return (Enum) cached;
    }
}
