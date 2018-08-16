
/***************************************

	MATRIX
		Neville Mehta

****************************************/


#pragma once


#include <exception>
#include <iostream>
#include <valarray>
#include "common.h"


class MatrixException : public exception
{	string error;

	public:
		MatrixException (const string& error = "") : error("\nMatrix Exception: " + error) {}
		const char* what () const throw() { return error.c_str(); }
		~MatrixException () throw() {}
};


template <typename T> class matrix
{	protected:
		unsigned _num_rows, _num_cols;
		valarray<T>* _data;

	public:
		explicit matrix (const unsigned& num_rows = 1, const unsigned& num_cols = 1) : _num_rows(num_rows), _num_cols(num_cols), _data(new valarray<T>(T(), num_rows * num_cols)) {}

		matrix (const unsigned& num_rows, const unsigned& num_cols, const T& val) : _num_rows(num_rows), _num_cols(num_cols), _data(new valarray<T>(val, _num_rows * _num_cols)) {}

		matrix (const unsigned& num_rows, const unsigned& num_cols, const T* v) : _num_rows(num_rows), _num_cols(num_cols), _data(new valarray<T>(v, _num_rows * _num_cols)) {}

		matrix (const unsigned& num_rows, const unsigned& num_cols, const valarray<T>& v) : _num_rows(num_rows), _num_cols(num_cols)
		{	if (v.size() != _num_rows * _num_cols)
				throw MatrixException("Illegal matrix construction.");
			else
				_data = new valarray<T>(v);
		}

		matrix (const matrix& m) : _num_rows(m._num_rows), _num_cols(m._num_cols), _data(new valarray<T>(*m._data)) {}

		const valarray<T> data () const { return *_data; }
		unsigned num_rows () const { return _num_rows; }
		unsigned num_cols () const { return _num_cols; }
		unsigned size () const { return _num_rows * _num_cols; }

		void resize (const unsigned& num_rows, const unsigned& num_cols)
		{	valarray<T> temp(*_data);   // Saves the old data before the resizing operation wipes it completely
			_data->resize(num_rows * num_cols);

			// Direct assignment of differing-size valarrays has undefined behavior
			unsigned min_size = _data->size() < temp.size() ? _data->size() : temp.size();
			for (unsigned i = 0; i < min_size; ++i)
				(*_data)[i] = temp[i];

			_num_rows = num_rows;
			_num_cols = num_cols;
		}

		void remove_row (const unsigned& row_index)
		{	for (unsigned r = row_index + 1; r < _num_rows; ++r)
				for (unsigned c = 0; c < _num_cols; ++c)
					(*_data)[(r - 1) * _num_cols + c] = (*_data)[r * _num_cols + c];
			resize(_num_rows - 1, _num_cols);
		}

		void remove_column (const unsigned& column_index)
		{	for (unsigned r = 0; r < _num_rows; ++r)
				for (unsigned c = 0; c < _num_cols; ++c)
					if (c != column_index)
						(*_data)[r * (_num_cols - 1) + (c > column_index ? c - 1 : c)] = (*_data)[r * _num_cols + c];
			resize(_num_rows, _num_cols - 1);
		}

		T operator () (const unsigned& r, const unsigned& c) const
		{	if (r >= _num_rows || c >= _num_cols)
				throw MatrixException("Matrix indices out of bounds.");
			return (*_data)[r * _num_cols + c];
		}
 
		T& operator () (const unsigned& r, const unsigned& c)
		{	if (r >= _num_rows || c >= _num_cols)
				throw MatrixException("Matrix indices out of bounds.");
			return (*_data)[r * _num_cols + c];
		}

		const valarray<T> operator [] (const unsigned& row) const { return (*_data)[slice(row * _num_cols, _num_cols, 1)]; }
		void set_row (const unsigned& row, const valarray<T>& v)
		{	if (_num_cols != v.size())
				throw MatrixException("Dimensions not matched for row assignment.");
			(*_data)[slice(row * _num_cols, _num_cols, 1)] = v;
		}

		const valarray<T> col (const unsigned& column) const { return (*_data)[slice(column, _num_rows, _num_cols)]; }
		void set_col (const unsigned& column, const valarray<T>& v)
		{	if (_num_rows != v.size())
				throw MatrixException("Dimensions not matched for column assignment.");
			return (*_data)[slice(column, _num_rows, _num_cols)] = v;
		}

		// Scalar assignment
		matrix<T>& operator = (const T& val)
		{	*_data = val;
			return *this;
		}

		// Matrix assignment
		matrix<T>& operator = (const matrix<T>& m)
		{	if (this != &m)
			{	if (_num_rows != m._num_rows || _num_cols != m._num_cols)
					throw MatrixException("Dimensions not matched for assignment.");
				*_data = *m._data;
			}
			return *this;
		}

		// Equals
		bool operator == (const matrix<T>& m) const
		{	if (_num_rows == m._num_rows && _num_cols == m._num_cols)
				return (*_data == *m._data).min();
			return false;
		}

		bool operator != (const matrix<T>& m) const
		{	return !(*this == m);
		}

		// Transpose
		matrix<T> operator ~ () const
		{	matrix<T> result(_num_cols, _num_rows);
			for (unsigned i = 0; i < _num_rows; ++i)
				result.set_col(i, (*this)[i]);
			return result;
		}

		T sum () const
		{	return _data->sum();
		}

		T min () const
		{	return _data->min();
		}

		T max () const
		{	return _data->max();
		}

		// Addition
		matrix<T> operator + (const matrix<T>& m) const
		{	if (_num_rows != m._num_rows || _num_cols != m._num_cols)
				throw MatrixException("Dimensions not matched for addition.");
			return matrix<T>(_num_rows, _num_cols, (*_data) + *m._data);
		}

		// Add & assign
		matrix<T>& operator += (const matrix<T>& m)
		{	if (_num_rows != m._num_rows || _num_cols != m._num_cols)
				throw MatrixException("Dimensions not matched for addition.");
			*_data += *m._data;
			return *this;
		}

		// Subtraction
		matrix<T> operator - (const matrix<T>& m) const
		{	if (_num_rows != m.num_rows() || _num_cols != m.num_cols())
				throw MatrixException("Dimensions not matched for subtraction.");
			return matrix<T>(_num_rows, _num_cols, (*_data) - m.data());
		}

		// Subtract & assign
		matrix<T>& operator -= (const matrix<T>& m)
		{	if (_num_rows != m._num_rows || _num_cols != m._num_cols)
				throw MatrixException("Dimensions not matched for subtraction.");
			*_data -= *m._data;
			return *this;
		}

		// Multiplication by scalar
		matrix<T> operator * (const T& val) const
		{	return matrix<T>(_num_rows, _num_cols, (*_data) * val);
		}

		// Multiplication by scalar (*=)
		matrix<T>& operator *= (const T& val)
		{	*_data *= val;
			return *this;
		}

		// Multiplication by vector
		matrix<T> operator * (const valarray<T>& v) const
		{	if (_num_cols != v.size())
				throw MatrixException("Dimensions not matched for multiplication.");

			matrix<T> result(_num_rows, 1);
			for (unsigned i = 0; i < _num_rows; ++i)
				result(i, 0) = ((*this)[i] * v).sum();
			return result;
		}

		// Multiplication by matrix
		matrix<T> operator * (const matrix<T>& m) const
		{	if (_num_cols != m._num_rows)
				throw MatrixException("Dimensions not matched for multiplication.");

			matrix<T> result(_num_rows, m._num_cols);
			for (unsigned i = 0; i < _num_rows; ++i)
				for (unsigned j = 0; j < m._num_cols; ++j)
					result(i, j) = ((*this)[i] * m.col(j)).sum();
			return result;
		}

		// Division by scalar
		matrix<T> operator / (const T& val) const
		{	return matrix<T>(_num_rows, _num_cols, (*_data) / val);
		}

		// Division by scalar (/=)
		matrix<T>& operator /= (const T& val)
		{	*_data /= val;
			return *this;
		}

		// Component-wise division by vector
		matrix<T> operator / (const valarray<T>& v) const
		{	if (_num_rows != v.size())
				throw MatrixException("Dimensions not matched for component-wise division.");

			matrix<T> result(_num_rows, _num_cols);
			for (unsigned i = 0; i < _num_rows; ++i)
				result.set_row(i, (*this)[i] / v[i]);
			return result;
		}

		~matrix () { delete _data; }
};


// Absolute
template <typename T> inline matrix<T> abs (const matrix<T>& m)
{	return matrix<T>(m.num_rows(), m.num_cols(), abs(m.data()));
}


// Multiplication by scalar
template <typename T> inline matrix<T> operator * (const T& val, const matrix<T>& m)
{	return m * val;
}


template <typename T> istream& operator >> (istream& istr, matrix<T>& m)
{	for (unsigned i = 0; i < m.num_rows(); ++i)
		for (unsigned j = 0; j < m.num_cols(); ++j)
			istr >> m(i, j);

	return istr;
}


template <typename T> ostream& operator << (ostream& out, const matrix<T>& m)
{	for (unsigned i = 0; i < m.num_rows(); ++i)
	{	out << "\n[";
		for (unsigned j = 0; j < m.num_cols(); ++j)
			out << ' ' << m(i, j);
		out << " ]";
	}

	return out;
}
