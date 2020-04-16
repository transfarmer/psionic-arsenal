/*Copyright 2019 Dudblockman

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.*/
package transfarmer.psionicarsenal.spell.trick.entity;

import net.minecraft.entity.Entity;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.common.core.handler.AdditiveMotionHandler;

public class PieceTrickRedirectMotion extends PieceTrick {
    SpellParam target;
    SpellParam axis;
    SpellParam angle;

    public PieceTrickRedirectMotion(Spell spell) {
        super(spell);
    }

    public static void addMotion(SpellContext context, Entity e, Vector3 ax, double ang) throws SpellRuntimeException {
        context.verifyEntity(e);
        if (!context.isInRadius(e)) {
            throw new SpellRuntimeException("psi.spellerror.outsideradius");
        } else {
            Vector3 entityVelocity = new Vector3(e.motionX, e.motionY, e.motionZ);
            Vector3 newMotion = entityVelocity.copy().rotate(ang, ax).subtract(entityVelocity);
            String key = "psi:Entity" + e.getEntityId() + "Motion";
            double x = 0.0D;
            double y = 0.0D;
            double z = 0.0D;
            String keyv;
            if (Math.abs(newMotion.x) > 1.0E-4D) {
                keyv = key + "X";
                if (!context.customData.containsKey(keyv)) {
                    x += newMotion.x;
                    context.customData.put(keyv, 0);
                }
            }

            if (Math.abs(newMotion.y) > 1.0E-4D) {
                keyv = key + "Y";
                if (!context.customData.containsKey(keyv)) {
                    y += newMotion.y;
                    context.customData.put(keyv, 0);
                }

                if (e.motionY >= 0.0D) {
                    e.fallDistance = 0.0F;
                }
            }

            if (Math.abs(newMotion.z) > 1.0E-4D) {
                keyv = key + "Z";
                if (!context.customData.containsKey(keyv)) {
                    z += newMotion.z;
                    context.customData.put(keyv, 0);
                }
            }

            AdditiveMotionHandler.addMotion(e, x, y, z);
        }
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        Double angleVal = this.getParamEvaluation(this.angle);
        if (angleVal == null) {
            angleVal = 1.0D;
        }

        double absAngle = Math.abs(angleVal);
        meta.addStat(EnumSpellStat.POTENCY, (int) this.multiplySafe(absAngle, new double[]{absAngle, 25.0D}));
        meta.addStat(EnumSpellStat.COST, (int) this.multiplySafe(absAngle, new double[]{100.0D}));
    }

    @Override
    public void initParams() {
        this.addParam(this.target = new ParamEntity("psi.spellparam.target", SpellParam.YELLOW, false, false));
        this.addParam(this.axis = new ParamVector("psi.spellparam.direction", SpellParam.GREEN, false, false));
        this.addParam(this.angle = new ParamNumber("psi.spellparam.speed", SpellParam.RED, false, true));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Entity targetVal = this.getParamValue(context, this.target);
        Vector3 axisVal = this.getParamValue(context, this.axis);
        Double angleVal = this.getParamValue(context, this.angle);
        addMotion(context, targetVal, axisVal, angleVal);
        return null;
    }
}
