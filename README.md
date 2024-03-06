# SHACL2FOL

The SHACL2FOL tool allows you to convert SHACL into first order logic sentences in the SCL language (described here: [Paolo Pareti, George Konstantinidis, Fabio Mogavero. Satisfiability and Containment of Recursive SHACL. Journal of Web Semantics (2022)](https://doi.org/10.1016/j.websem.2022.100721) ) and compute the answer to a number of decision problems.

It is developed by [Paolo Pareti ](https://paolopareti.uk/)

Currently, it supports three modes:

* In the satisfiability mode, SHACL2FOL transforms a shape graph into a first order logic theory that is satisfiable iff the original shape graph is satisfiable.
* In the containment mode, SHACL2FOL transforms two shape graphs into a first order logic theory that is UNsatisfiable iff the first shape graph is contained in the second.
* In the validity mode, SHACL2FOL transforms a shape graph and a data graph into a first order logic theory that is satisfiable iff the shape graph validates the data graph.

The outputs of this tool are two:

* Firstly, the desired first order logic theory in the TPTP format: http://www.tptp.org/
* Secondly, the answer to the satisfiability/containment/validation decision problems, by applying a desired theorem prover.

## Installation

This software was tested on Ubuntu, and depending on the theorem prover you decide to use, it might not work on Windows.

1. Install a theorem prover. The recommended one is Vampire [http://www.vprover.org/](https://vprover.github.io/) .
2. Configure the config.properties file with the path to the theorem prover executable .
3. Run the SHACL2FOL.jar jar file with the following arguments:

To perform a satisfiability check:
* arg[0] the letter 's'
* arg[1] the path to the shape graph to check for satisfiability

To perform a containment check (does the first shape graph contain the second?):
* arg[0] the letter 'c'
* arg[1] the path to the first shape graph
* arg[2] the path to the second shape graph

To perform a validation check:
* arg[0] the letter 'v'
* arg[1] the path to the shape graph
* arg[2] the path to the data graph

## Using a TPTP file with the E theorem prover

You can evaluate the satisfiability of a FOL theory in TPTP format using the E theorem prover.

In config.properties, change the tptpPrefix to input_formula and encodeUNA to true

After installing E, you can run the satisfiability check running the following command in the `PROVER` subfolder (assuming your tptp file is called `test.tptp`)
* `./eprover test.tptp`

If your TPTP file is satisfiable (and thus if the original SHACL document is satisfiable), the following line will be printed on console: `SZS status Satisfiable`; or else you will see `SZS status Unsatisfiable`.

## Limitations

* Out of the filter components, only the sh:NodeKind has been implemented. All of the other SHACL core constraint components and target components have been implemented. 
* All shapes must be IRIs, and not blank nodes. For example, instead of creating a property constraint using (in Turtle syntax) `:shape_A sh:property [ ...XYZ... ]`, create a specific IRI for that property instead, which is defined later as usual `:shape_A sh:property :PROP_A . :PROP_A ...XYZ... `. 

## Sample Usage

The `runnable` folder contains a number of sample shape and data graphs:
* `StudentShapes.ttl`, `M1.ttl`, `M2.ttl` and `M3.ttl` are shape graphs
* `StudentGraphSat.ttl` and `StudentGraphUnsat.ttl` are data graphs

### Satisfiability checks

Command: `java -jar SHACL2FOL.jar s M1.ttl`
Output: 
```
Default prover command ./vampire
Default file where to store the TPTP output ./testOUT.tptp
These defaults can be changed in the config.properties configuration file.
Performing Satisfiability check of M1.ttl
Is satisfiable? true
Memory (KB) 4861
Time (s) 0.018
```

Command: `java -jar SHACL2FOL.jar s M3.ttl`
Output: 
```
Default prover command ./vampire
Default file where to store the TPTP output ./testOUT.tptp
These defaults can be changed in the config.properties configuration file.
Performing Satisfiability check of M3.ttl
Is satisfiable? false
Memory (KB) 4989
Time (s) 0.02
```

Command: `java -jar SHACL2FOL.jar s M4.ttl`
Output: 
```
Default prover command ./vampire
Default file where to store the TPTP output ./testOUT.tptp
These defaults can be changed in the config.properties configuration file.
Performing Satisfiability check of M4.ttl
Is satisfiable? false
Memory (KB) 4989
Time (s) 0.034
```

### Containment checks

Command: `java -jar SHACL2FOL.jar c M1.ttl M2.ttl`
Output:
```
Default prover command ./vampire
Default file where to store the TPTP output ./testOUT.tptp
These defaults can be changed in the config.properties configuration file.
Does shape graph M1.ttl
... contain shape graph M2.ttl?
Is the first shape graph contained in the second? true
Whenever a data graph is validated by the first, it is also validated by the second.
Memory (KB) 4989
Time (s) 0.018
```

Command: `java -jar SHACL2FOL.jar c M2.ttl M1.ttl`
Output:
```
efault prover command ./vampire
Default file where to store the TPTP output ./testOUT.tptp
These defaults can be changed in the config.properties configuration file.
Does shape graph M2.ttl
... contain shape graph M1.ttl?
Is the first shape graph contained in the second? false
There exists a data graph validated by the first but not by the second.
Memory (KB) 4989
Time (s) 0.022
```

### Validation checks

Command: `java -jar SHACL2FOL.jar v StudentShapes.ttl StudentGraphSat.ttl`
```
Default prover command ./vampire
Default file where to store the TPTP output ./testOUT.tptp
These defaults can be changed in the config.properties configuration file.
Performing Validity check of data graph StudentGraphSat.ttl
... with respect to shape graph StudentShapes.ttl
Is data graph valid? true
Memory (KB) 4989
Time (s) 0.019
```

Command `java -jar SHACL2FOL.jar v StudentShapes.ttl StudentGraphUnsat.ttl`
```
Default prover command ./vampire
Default file where to store the TPTP output ./testOUT.tptp
These defaults can be changed in the config.properties configuration file.
Performing Validity check of data graph StudentGraphUnsat.ttl
... with respect to shape graph StudentShapes.ttl
Is data graph valid? false
Memory (KB) 4989
Time (s) 0.018
```

## Useful links:
* Vampire theorem prover [http://www.vprover.org/](https://vprover.github.io/)
* E Prover homepage https://wwwlehre.dhbw-stuttgart.de/~sschulz/E/E.html
* TPTP Syntax http://www.tptp.org/TPTP/SyntaxBNF.html
* TPTP Format Problem http://www.tptp.org/TPTP/QuickGuide/Problems.html
* E Prover manual http://wwwlehre.dhbw-stuttgart.de/~sschulz/WORK/E_DOWNLOAD/V_2.6/eprover.pdf
* E Prover installation instructions http://wwwlehre.dhbw-stuttgart.de/~sschulz/WORK/E_DOWNLOAD/V_2.6/README
