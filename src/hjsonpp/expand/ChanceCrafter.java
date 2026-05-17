package hjsonpp.expand;

import arc.math.Mathf;
import arc.util.io.Reads;
import arc.util.io.Writes;
import hjsonpp.expand.meta.AdditionalStats;
import mindustry.world.blocks.production.GenericCrafter;

public class ChanceCrafter extends GenericCrafter{
    // what chance it to produce item/liquid. 1 = 100%
    public float produceChance = 0.5f;

    public ChanceCrafter(String name){
        super(name);
    }

    @Override
    public void setStats(){
        stats.add(AdditionalStats.produceChance, produceChance * 100f + "%");
        super.setStats();
    }

    public class ChanceCrafterBuild extends  GenericCrafterBuild{
        public int seed;

        @Override
        public void created(){
            seed = Mathf.randomSeed(tile.pos(), 0, Integer.MAX_VALUE - 1);
        }

        public boolean chanced(float chance){
            return Mathf.randomSeed(seed, 1) < chance;
        }

        @Override
        public void craft(){
           consume();
            if(outputItems != null & chanced(produceChance)){
                for(var output : outputItems){
                    for(int i = 0; i < output.amount; i++){
                        offload(output.item);
                    }
                }
            }
            if(wasVisible & chanced(produceChance)){
                craftEffect.at(x, y);
            }
            progress %= 1f;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(seed);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            if(revision == 1) seed = read.i();
        }
    }
}
