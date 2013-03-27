

#ifndef ZOMBIE_H
#define ZOMBIE_H

class CPlayer;
class CEntity;

bool CheckZombie(int i, int j);
void Reddening();
void Grasp(CPlayer* zom, CEntity* zE, CPlayer* hum);

#endif