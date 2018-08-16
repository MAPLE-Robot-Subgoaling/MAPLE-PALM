
/***************************************

	GRAPH
		Neville Mehta

****************************************/


#pragma once


#include <algorithm>
#include <exception>
#include <iostream>
#include <queue>
#include <stack>
#include <valarray>
#include "matrix.h"


class GraphException : public exception
{	private:
		string error;

	public:
		GraphException (const string& error = "") : error("Graph Exception: " + error) {}
		const char* what () const throw() { return error.c_str(); }
		~GraphException () throw() {}
};


template <typename T> class graph
{	matrix<T*> _adj_mat;   // Adjacency matrix

	set<set<unsigned>> tarjan (const unsigned& i, int& index, vector<int>& v_low, vector<int>& v_index, list<unsigned>& v_stack) const
	{	set<set<unsigned>> scc;
		v_low[i] = index;
		v_index[i] = index;
		++index;
		v_stack.push_back(i);

		set<unsigned> child_set = children(i);
		for (auto n_itr = child_set.cbegin(); n_itr != child_set.cend(); ++n_itr)
		{	if (v_index[*n_itr] == -1)
			{	set<set<unsigned>> sub_scc = tarjan(*n_itr, index, v_low, v_index, v_stack);
				scc.insert(sub_scc.begin(), sub_scc.end());
				v_low[i] = min(v_low[i], v_low[*n_itr]);
			}
			else if (find(v_stack.begin(), v_stack.end(), *n_itr) != v_stack.end())
				v_low[i] = min(v_low[i], v_index[*n_itr]);
		}

		if (v_low[i] == v_index[i])
		{	set<unsigned> component;
			unsigned j;
			do
			{	j = v_stack.back();
				component.insert(j);
				v_stack.pop_back();
			} while (j != i);
			scc.insert(component);
		}
		return scc;
	}

	// Composition of graphs (relations) g2 o g1: if there exists y such that (x,y) in g1 and (y,z) in g2, then (x,z) is in the result
	graph<T> composition (const graph<T>& g, const T& edge_label) const
	{	graph<T> gc(num_vertices());
		for (unsigned i = 0; i < num_vertices(); ++i)
			for (unsigned j = 0; j < num_vertices(); ++j)
				for (unsigned k = 0; k < num_vertices(); ++k)
					if (g.edge_exists(i, k, edge_label) && edge_exists(k, j, edge_label))
					{	gc.set_edge(i, j, edge_label);
						break;
					}
		return gc;
	}

	public:
		explicit graph (const unsigned& num_vertices = 1) : _adj_mat(matrix<T*>(num_vertices, num_vertices, static_cast<T*>(nullptr))) {}

		graph (const graph& g) : _adj_mat(matrix<T*>(g.num_vertices(), g.num_vertices(), static_cast<T*>(nullptr)))
		{	for (unsigned i = 0; i < num_vertices(); ++i)
				for (unsigned j = 0; j < num_vertices(); ++j)
					if (g._adj_mat(i,j))
						_adj_mat(i,j) = new T(*g._adj_mat(i,j));
		}

		graph<T>& operator = (const graph& g)
		{	if (this != &g)
			{	for (unsigned i = 0; i < num_vertices(); ++i)
					for (unsigned j = 0; j < num_vertices(); ++j)
						if (_adj_mat(i, j))
						{	delete _adj_mat(i, j);
							_adj_mat(i, j) = nullptr;
						}

				_adj_mat.resize(g.num_vertices(), g.num_vertices());
				for (unsigned i = 0; i < num_vertices(); ++i)
					for (unsigned j = 0; j < num_vertices(); ++j)
						if (g._adj_mat(i,j))
							_adj_mat(i,j) = new T(*g._adj_mat(i,j));
			}
			return *this;
		}

		unsigned num_vertices () const { return _adj_mat.num_rows(); }

		void resize (const unsigned& num_vertices) { _adj_mat.resize(num_vertices, num_vertices); }

		void set_edge (const unsigned& v1, const unsigned& v2, const T& edge_label = T())
		{	if (v1 >= num_vertices() || v2 >= num_vertices())
				throw GraphException("Vertex indices out of bounds.");
			if (!_adj_mat(v1, v2))
				_adj_mat(v1, v2) = new T;
			*_adj_mat(v1, v2) = edge_label;
		}

		bool edge_exists (const unsigned& v1, const unsigned& v2) const
		{	if (v1 >= num_vertices() || v2 >= num_vertices())
				throw GraphException("Vertex indices out of bounds.");
			return _adj_mat(v1, v2) != nullptr;
		}

		bool edge_exists (const unsigned& v1, const unsigned& v2, const T& edge_label) const
		{	if (v1 >= num_vertices() || v2 >= num_vertices())
				throw GraphException("Vertex indices out of bounds.");
			return _adj_mat(v1, v2) && *_adj_mat(v1, v2) == edge_label;
		}

		T& edge (const unsigned& v1, const unsigned& v2) const
		{	if (v1 >= num_vertices() || v2 >= num_vertices())
				throw GraphException("Vertex indices out of bounds.");
			if (!_adj_mat(v1, v2))
				throw GraphException("Edge does not exist.");
			return *_adj_mat(v1, v2);
		}

		void remove_edge (const unsigned& v1, const unsigned& v2)
		{	if (v1 >= num_vertices() || v2 >= num_vertices())
				throw GraphException("Vertex indices out of bounds.");
			if (_adj_mat(v1, v2))
			{	delete _adj_mat(v1, v2);
				_adj_mat(v1, v2) = nullptr;
			}
		}

		void remove_edge (const unsigned& v1, const unsigned& v2, const T& edge_label)
		{	if (v1 >= num_vertices() || v2 >= num_vertices())
				throw GraphException("Vertex indices out of bounds.");
			if (edge_exists(v1, v2, edge_label))
			{	delete _adj_mat(v1, v2);
				_adj_mat(v1, v2) = nullptr;
			}
		}

		void remove_node (const unsigned& v)
		{	// Remove all arcs associate with v
			for (unsigned i = 0; i < num_vertices(); ++i)
			{	if (_adj_mat(v, i))
				{	delete _adj_mat(v, i);
					_adj_mat(v, i) = nullptr;
				}
				if (_adj_mat(i, v))
				{	delete _adj_mat(i, v);
					_adj_mat(i, v) = nullptr;
				}
			}

			_adj_mat.remove_row(v);
			_adj_mat.remove_column(v);
		}

		set<unsigned> children (const unsigned& v) const
		{	set<unsigned> neighbors;
			for (unsigned w = 0; w < num_vertices(); ++w)
				if (_adj_mat(v, w))
					neighbors.insert(w);
			return neighbors;
		}

		set<unsigned> children (const unsigned& v, const T& edge_label) const
		{	set<unsigned> neighbors;
			for (unsigned w = 0; w < num_vertices(); ++w)
				if (edge_exists(v, w, edge_label))
					neighbors.insert(w);
			return neighbors;
		}

		set<unsigned> parents (const unsigned& v) const
		{	set<unsigned> neighbors;
			for (unsigned w = 0; w < num_vertices(); ++w)
				if (_adj_mat(w, v))
					neighbors.insert(w);
			return neighbors;
		}

		set<unsigned> parents (const unsigned& v, const T& edge_label) const
		{	set<unsigned> neighbors;
			for (unsigned w = 0; w < num_vertices(); ++w)
				if (edge_exists(w, v, edge_label))
					neighbors.insert(w);
			return neighbors;
		}

		// Returns empty vector if no path exists
		vector<unsigned> shortest_path (const unsigned& start_vertex, const unsigned& end_vertex) const
		{	if (start_vertex == end_vertex)
				return vector<unsigned>(1, start_vertex);

			vector<int> parent(num_vertices(), -1);
			parent[start_vertex] = start_vertex;
			queue<unsigned> next_vertex;
			next_vertex.push(start_vertex);
			while (!next_vertex.empty() && next_vertex.front() != end_vertex)
			{	unsigned i = next_vertex.front();
				next_vertex.pop();
				for (unsigned j = 0; j < num_vertices(); ++j)
					if (edge_exists(i, j) && parent[j] == -1)
					{	parent[j] = i;
						next_vertex.push(j);
					}
			}

			vector<unsigned> sh_path;
			if (!next_vertex.empty() && next_vertex.front() == end_vertex)   // Path exists
			{	sh_path.push_back(end_vertex);
				unsigned i = end_vertex;
				while (i != start_vertex)
				{	sh_path.insert(sh_path.begin(), parent[i]);
					i = parent[i];
				}
			}
			return sh_path;
		}

		// Returns empty vector if no path exists
		vector<unsigned> shortest_path (const unsigned& start_vertex, const unsigned& end_vertex, const T& edge_label) const
		{	if (start_vertex == end_vertex)
				return vector<unsigned>(1, start_vertex);

			vector<int> parent(num_vertices(), -1);
			parent[start_vertex] = start_vertex;
			queue<unsigned> next_vertex;
			next_vertex.push(start_vertex);
			while (!next_vertex.empty() && next_vertex.front() != end_vertex)
			{	unsigned i = next_vertex.front();
				next_vertex.pop();
				for (unsigned j = 0; j < num_vertices(); ++j)
					if (edge_exists(i, j, edge_label) && parent[j] == -1)
					{	parent[j] = i;
						next_vertex.push(j);
					}
			}

			vector<unsigned> sh_path;
			if (!next_vertex.empty() && next_vertex.front() == end_vertex)   // Path exists
			{	sh_path.push_back(end_vertex);
				unsigned i = end_vertex;
				while (i != start_vertex)
				{	sh_path.insert(sh_path.begin(), parent[i]);
					i = parent[i];
				}
			}
			return sh_path;
		}

		// Set of vertex sets of strongly connected components
		set<set<unsigned>> strongly_connected_components () const
		{	set<set<unsigned>> all_sccs;
			vector<int> v_low(num_vertices(), 0);
			vector<int> v_index(num_vertices(), -1);

			for (unsigned v = 0; v < num_vertices(); ++v)
				if (v_index[v] == -1)
				{	int index = 0;
					list<unsigned> v_stack;
					set<set<unsigned>> scc = tarjan(v, index, v_low, v_index, v_stack);
					all_sccs.insert(scc.begin(), scc.end());
				}

			return all_sccs;
		}

		bool is_strongly_connected () const
		{	vector<int> v_low(num_vertices(), 0);
			vector<int> v_index(num_vertices(), -1);
			list<unsigned> v_stack;
			int index = 0;

			set<set<unsigned>> scc = tarjan(0, index, v_low, v_index, v_stack);   // Only need to check one vertex
			if (scc.begin()->size() == num_vertices())
				return true;
			return false;
		}

		bool is_acyclic () const
		{	for (unsigned i = 0; i < num_vertices(); ++i)
				if (edge_exists(i, i))
					return false;
			return strongly_connected_components().size() == num_vertices();
		}

		// Transitive closure of edges with the specified label (Warshall's algorithm in place)
		void transitive_closure (const T& edge_label)
		{	for (unsigned j = 0; j < num_vertices(); ++j)
				for (unsigned i = 0; i < num_vertices(); ++i)
					if (i != j && edge_exists(i, j, edge_label))
						for (unsigned k = 0; k < num_vertices(); ++k)
							if (k != j && !edge_exists(i, k, edge_label) && edge_exists(j, k, edge_label))
								set_edge(i, k, edge_label);
		}

		// Transitive reduction = g - (g o g*) where g* is the transitive closure
		void transitive_reduction (const T& edge_label)
		{	graph<T> g(*this);
			g.transitive_closure(edge_label);
			g = composition(g, edge_label);

			for (unsigned i = 0; i < num_vertices(); ++i)
				for (unsigned j = 0; j < num_vertices(); ++j)
					if (g.edge_exists(i, j, edge_label))
						remove_edge(i, j, edge_label);
		}

		void print_gv (const string& filename, const bool& edge_label = false) const
		{	ofstream gfile(filename.c_str());
			if (!gfile.is_open())
				throw GraphException("Unable to write to file.");

			gfile << "digraph {\nedge [arrowhead=empty]\n\n";
			for (unsigned i = 0; i < num_vertices(); ++i)
				for (unsigned j = 0; j < num_vertices(); ++j)
					if (_adj_mat(i, j))
					{	gfile << i << " -> " << j;
						if (edge_label)
							gfile << " [label=\"" << *_adj_mat(i,j) << "\"]\n";
						else
							gfile << endl;
					}
			gfile << "}\n";
			gfile.close();
		}

		~graph ()
		{	for (unsigned i = 0; i < num_vertices(); ++i)
				for (unsigned j = 0; j < num_vertices(); ++j)
					if (_adj_mat(i, j))
						delete _adj_mat(i, j);
		}
};


template <typename T> istream& operator >> (istream& istr, graph<T>& g)
{	T edge_label;
	for (unsigned i = 0; i < g.num_vertices(); ++i)
		for (unsigned j = 0; j < g.num_vertices(); ++j)
		{	istr >> edge_label;
			g.set_edge(i, j, edge_label);
		}

	return istr;
}


template <typename T> ostream& operator << (ostream& out, const graph<T>& g)
{	out << "Vertices = {";
	for (unsigned i = 0; i < g.num_vertices(); ++i)
	{	if (i > 0)
			out << ", ";
		out << i;
	}
	out << "}\nEdges = {";

	for (unsigned i = 0; i < g.num_vertices(); ++i)
	{	for (unsigned j = 0; j < g.num_vertices(); ++j)
			if (g.edge_exists(i, j))
				out << "\n" << i << " -> " << j << ":\n" << g.edge(i, j);
	}
	out << "\n}\n";

	return out;
}
