README :
	Nom du plugin : NPCJobsShop
	Version : 1.0
	Description : NPCJobsShop permet d'acheter ou de vendre à un NPC certaines ressources prédéfinis, via une interface du type inventaire
			Chaque NPC à sa propre table où sont stockés les items qu'il vend ou achète (le stockage des données se fait exclusivement via une base de données SQL)
			Vous pouvez donc créer un nouveau NPC à l'endroit où vous vous tenez et ainsi ajouter des items à son marché (table)
			Le joueur pourra ainsi cliquer sur le NPC pour voir son marché et ainsi naviguer et vendre et acheter des items
			Dans une prochaine MAJ, le niveau de métier du joueur sera utiliser afin que celui-ci ait des prix avantageux
			
			Hiéarchie BDD:
				Une table par NPC avec `item`, `prix(unitaire)`, `quantité(-1 pour vente)`
				Une table répertoriant tous les NPC ainsi que leur métier affilié (WORK IN PROGRESS)
	
	Commandes :
		npcjobs :
			addnpc <nomNPC> <jobs> : ajoute un npc à l'endroit où se tient le joueur ainsi que dans la base de donnée
			removenpc <nomNPC> : supprime un npc de la base de donnée uniquement (il faudra tuer le NPC manuellement)
			additem <nomNPC> <nomItem> <prix> (<quantité>) : ajout de <nomItem> (pas de minecraft:dirt => dirt) dans la table <nomNPC> avec <prix> et <quantité>
															(si <quantité> n'est pas précisé mise à -1 pour vendre)
			removeitem <nomNPC> <nomItem> : enlève <nomItem> de la table <nomNPC>
			setprice <nomNPC> <nomItem> <prix> : change le prix d'un item dans la table correspondante
			setquantity <nomNPC> <nomItem> <quantite> : change la quantité d'un item dans la table correspondante
			
			getlist <nomNPC> : retourne toute la table <nomNPC> sous forme de String avec un espace entre chaque élément (surtout utilisé pour debuguer mais est aussi utile dans le code)
		
		npcshop <nomNPC> : ajout d'un NPC avec <nomNPC> à l'endroit où se trouve le joueur (utile quand on ne veut pas l'ajouter à la base de donnée)
			