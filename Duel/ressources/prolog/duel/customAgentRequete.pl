use_module(library(jpl)).

shotImpact(Probability):-
	Probability>0.1.

/* DECISIONS */

explore():-
	not(seeEnemy()).

toOpenFire(P):-
	shotImpact(P),
	seeEnemy().

attack():-
	seeEnemy().

seeEnemy():-
    jpl_call('sma.agents.customs.CustomAgent', enemyInSight, [], X),
    jpl_is_true(X).

/*
goodPosition(Px,Py,Pz):-
    jpl_call('sma.actionsBehaviours.customs.MyExploreBehavior', isAGoodPosition, [Px,Py,Pz], X),
    jpl_is_true(X).
*/
