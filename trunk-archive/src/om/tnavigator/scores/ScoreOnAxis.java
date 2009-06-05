package om.tnavigator.scores;

/**
 * Score details on single axis, that is, the actual score, and the maximum possible.
 */
class ScoreOnAxis
{
	double score;
	double max;
	ScoreOnAxis()
	{
	}
	ScoreOnAxis(ScoreOnAxis soa)
	{
		this(soa.score, soa.max);
	}
	ScoreOnAxis(double score, double max)
	{
		this.score = score;
		this.max = max;
	}
	void add(ScoreOnAxis soa)
	{
		add(soa.score, soa.max);
	}
	void add(double otherScore, double otherMax)
	{
		score += otherScore;
		max += otherMax;
	}
}