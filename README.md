# SHACL2FOL

The SHACL2FOL tool allows you to convert SHACL into first order logic sentences in the SCL language (described here: https://arxiv.org/abs/2108.13063 ) and compute the answer to a number of decision problems.

Currently, it supports three modes:

* In the validation mode, SHACL2FOL transforms a shape graph into a first order logic theory that is satisfiable iff the original shape graph is satisfiable.
* In the containment mode, SHACL2FOL transforms two shape graphs into a first order logic theory that is UNsatisfiable iff the first shape graph is contained in the second.
* In the validity mode, SHACL2FOL transforms a shape graph and a data graph into a first order logic theory that is satisfiable iff the shape graph validates the data graph.

The outputs of this tool are two:

* Firstly, the desired first order logic theory in the TPTP format: http://www.tptp.org/
* Secondly, the answer to the satisfiability/containment/validation decision problems, by applying a desired theorem prover.

## Installation

1. Install a theorem prover. The recommended one is Vampire http://www.vprover.org/ (you can get the latest release here https://github.com/vprover/vampire/releases , it was tested on Vampire 4.9).
2. Configure the config.properties file with the path to the theorem prover executable .
3. Make sure the theorem prover executable file has permission to be executed .
3. Run the SHACL2FOL.jar jar file with the following arguments:

To perform a satisfiability check:
* arg[0] the letter 's'
* arg[1] the path to the shape graph to check for satisfiability

To perform a containment check (does the first shape graph contain the second?):
* arg[0] the letter 'c'
* arg[1] the path to the first shape graph
* arg[2] the path to the second shape graph

To perform a validity check:
* arg[0] the letter 'v'
* arg[1] the path to the shape graph
* arg[2] the path to the data graph

To perform an action preservation check:
* arg[0] the letter 'a'
* arg[1] the path to the shape graph
* arg[2] the path to JSON filed containing the actions

To check for the final model property, configure the arguments of the prover command accordingly using the `proverArguments` property in `config.properties`.
For example, to look for final models in Vampire 4.9 this property can be set to: 

```
proverArguments=--saturation_algorithm fmb
```

## Action JSON Data Model

Actions should be specified as a JSON file containing a list of action objects, meant to be performed in the given order. Strings containing IRIs or RDF shape should be appropriately escaped to be valid JSON strings. Please see `actions1.json` in the Runnable folder a valid example. Two action types are defined:

*Path Actions*

This action adds/removes a triple x r y, whenever the graph contains a path p from x to y. This object must have four fields:

* `"type": "PathAction"`
* `isAdd` should be True if the triples should be added, false if they should be removed
* `predicate` the full IRI of the predicate r of the triples to be added
* `path` SHACL property path p

*SHAPE Actions*

This action adds/removes a triple x r y, between all the nodes x in the graph that satisfy Shape1, and all the nodes y that satisfy Shape2. This object must have five fields:

* `"type": "ShapeAction"`
* `isAdd` should be True if the triples should be added, false if they should be removed
* `predicate` the full IRI of the predicate r of the triples to be added
* `subjectShape` a string representation of Shape1
* `objectShape` a string representation of Shape2


## Using a TPTP file with the E theorem prover

You can evaluate the satisfiability of a FOL theory in TPTP format using the E theorem prover.

In config.properties, change the tptpPrefix to input_formula and encodeUNA to true

After installing E, you can run the satisfiability check running the following command in the `PROVER` subfolder (assuming your tptp file is called `test.tptp`)
* `./eprover test.tptp`

If your TPTP file is satisfiable (and thus if the original SHACL document is satisfiable), the following line will be printed on console: `SZS status Satisfiable`; or else you will see `SZS status Unsatisfiable`.

## Limitations

Out of the filter components, only the sh:NodeKind has been implemented. All of the other SHACL core constraint components and target components have been implemented. 

## Useful links:
* E Prover homepage https://wwwlehre.dhbw-stuttgart.de/~sschulz/E/E.html
* TPTP Syntax http://www.tptp.org/TPTP/SyntaxBNF.html
* TPTP Format Problem http://www.tptp.org/TPTP/QuickGuide/Problems.html
* E Prover manual http://wwwlehre.dhbw-stuttgart.de/~sschulz/WORK/E_DOWNLOAD/V_2.6/eprover.pdf
* E Prover installation instructions http://wwwlehre.dhbw-stuttgart.de/~sschulz/WORK/E_DOWNLOAD/V_2.6/README
