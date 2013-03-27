
#ifndef pathogen_Physics_h
#define pathogen_Physics_h

#define FRICTION        3.0f
#define GRAVITY         9.8f

struct CVector3;
class CEntity;
class CEntityType;

void Physics();
int Collides2(int i);
CVector3 EMin(CEntity* e, CEntityType* t);

#endif