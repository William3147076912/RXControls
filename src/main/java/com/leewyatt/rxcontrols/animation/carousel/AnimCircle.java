/*
 * MIT License
 *
 * Copyright (c) 2021 LeeWyatt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package com.leewyatt.rxcontrols.animation.carousel;

import com.leewyatt.rxcontrols.controls.RXCarousel;
import com.leewyatt.rxcontrols.pane.RXCarouselPane;
import javafx.animation.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.List;

/**
 * @author LeeWyatt
 * QQ: 9670453
 * QQ群: 518914410
 *
 * 轮播图效果: 圆形逐渐显示
 */
public class AnimCircle extends CarouselAnimationBase {
    private double opacity;
    private Circle circleClip;
    private Timeline animation;

    public AnimCircle() {
        this(1.0);
    }

    public AnimCircle(double opacity) {
        this.opacity = opacity;
        circleClip = new Circle();
        animation = new Timeline();
    }

    @Override
    public Animation getAnimation(RXCarousel rxCarousel, StackPane contentPane, Pane effectPane, List<RXCarouselPane> panes, int currentIndex, int nextIndex, boolean foreAndAftJump, Duration animationTime) {
        reset(panes, nextIndex,currentIndex);
        RXCarouselPane currentPane = panes.get(currentIndex);
        RXCarouselPane nextPane = panes.get(nextIndex);

        double paneWidth = CarouselAnimUtil.getPaneWidth(contentPane);
        double paneHeight = CarouselAnimUtil.getPaneHeight(contentPane);

        circleClip.setCenterX(paneWidth / 2);
        circleClip.setCenterY(paneHeight / 2);
        // 外接圆半径
        double r = Math.sqrt(paneWidth * paneWidth + paneHeight * paneHeight) / 2;
        boolean direction = CarouselAnimUtil.computeDirection(panes, currentIndex, nextIndex, foreAndAftJump);
        KeyValue kv1, kv2, kv3, kv4;
        if (direction) {
            nextPane.setClip(circleClip);
            nextPane.toFront();
            //第一帧的keyValue
            kv1 = new KeyValue(circleClip.radiusProperty(), 0);
            kv2 = new KeyValue(currentPane.opacityProperty(), 1);
            //第二帧的keyValue
            kv3 = new KeyValue(currentPane.opacityProperty(), opacity);
            kv4 = new KeyValue(circleClip.radiusProperty(), r);
        } else {
            currentPane.setClip(circleClip);
            currentPane.toFront();
            //第一帧的keyValue
            kv1 = new KeyValue(circleClip.radiusProperty(), r);
            kv2 = new KeyValue(nextPane.opacityProperty(), opacity);
            //第二帧的keyValue
            kv3 = new KeyValue(nextPane.opacityProperty(), 1);
            kv4 = new KeyValue(circleClip.radiusProperty(), 0);
        }
        animation.getKeyFrames().setAll(
                new KeyFrame(Duration.ZERO, event -> {
                   nextPane.fireOpening();
                    currentPane.fireClosing();
                }, kv1, kv2),
                new KeyFrame(animationTime, kv3, kv4));
        animation.setOnFinished(e -> {
            nextPane.setClip(null);
            nextPane.setOpacity(1.0);
            nextPane.fireOpened();
            currentPane.setVisible(false);
            currentPane.setClip(null);
            currentPane.setOpacity(1.0);
            currentPane.fireClosed();
        });
        return animation;
    }

    @Override
    public void clearEffects(List<RXCarouselPane> panes, Pane effectPane, int currentIndex, int nextIndex) {
        reset(panes, nextIndex);
    }

    private void reset(List<RXCarouselPane> panes, int... showIndexAry) {
        for (int i = 0; i < panes.size(); i++) {
            RXCarouselPane pane = panes.get(i);
            CarouselAnimUtil.showPanes(i, pane, showIndexAry);
            pane.setClip(null);
            pane.setOpacity(1.0);
        }
    }

    @Override
    public void dispose() {
        CarouselAnimUtil.disposeTimeline(animation);
    }
}
