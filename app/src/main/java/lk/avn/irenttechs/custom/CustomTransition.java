package lk.avn.irenttechs.custom;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;

public class CustomTransition extends Transition {

    private static final String PROPERTY_HEIGHT = "customTransition:height";

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues transitionValues) {
        if (transitionValues.view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) transitionValues.view;
            int height = viewGroup.getHeight();
            transitionValues.values.put(PROPERTY_HEIGHT, height);
        }
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }

        View view = endValues.view;
        int startHeight = (int) startValues.values.get(PROPERTY_HEIGHT);
        int endHeight = (int) endValues.values.get(PROPERTY_HEIGHT);

        if (startHeight != endHeight) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(ObjectAnimator.ofInt(view, "height", startHeight, endHeight));
            return animatorSet;
        }

        return null;
    }
}
