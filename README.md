This is an implementation of the Fellegi-Sunter algorithm in Java used by other CSSIP projects. It also includes an implementation of the 1-to-1 link extraction algorithm.

Known issues:
* The convergence of the EM algorithm is erratic, most likely due to a bug in implementation. As an alternative, there is also a Bayesian method for fitting record linkage paraters, it is slower but the output is consistent.
*  The implementation of the Jaro-Winkler string comparison returns 0 for short strings (fewer that 4? characters). This is not a problem in the case of an exact match, because exact agreement is tested separately, but it means that the implementation should not be used independently. Consider replacing this with an external implementation.
