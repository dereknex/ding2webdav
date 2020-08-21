CREATE TABLE "department" ("id" INTEGER, "name" TEXT, "parentID" INTEGER, PRIMARY KEY("id"));


CREATE TABLE "user"( "id" TEXT, "avatar" TEXT, "department" TEXT, "email" TEXT, "hiredDate" TEXT, "mobile" TEXT, "name" TEXT, "active" int, "jobnumber" TEXT, "orgEmail" TEXT, "position" TEXT, "remark" TEXT, "tel" TEXT, "workPlace" TEXT, PRIMARY KEY(id) );
CREATE TABLE "migration" ("ver" INTEGER, "update_at" INTEGER, PRIMARY KEY("ver"));